package com.learning.langchain.lessons.l02_memory.orchestrator;

import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import com.learning.langchain.shared.memory.ConversationMemoryStore;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SqlAgentWithMemoryRunner {

    private final ChatModel model;
    private final SqlTool sqlTool;
    private final ConversationMemoryStore memoryStore;

    private static final Logger log =
            LoggerFactory.getLogger(SqlAgentWithMemoryRunner.class);

    private final ObjectMapper mapper = new ObjectMapper();


    public SqlAgentWithMemoryRunner(ChatModel model, SqlTool sqlTool, ConversationMemoryStore conversationStore) {
        this.model = model;
        this.sqlTool = sqlTool;
        this.memoryStore = conversationStore;
    }

    private List<ToolSpecification> toolSpecs() {

        return List.of(
                ToolSpecification.builder()
                        .name("listTables")
                        .description("List all tables in the SQLite database")
                        .build(),

                ToolSpecification.builder()
                        .name("describeTable")
                        .description("Describe columns of a table. Expects argument 'table'")
                        .build(),

                ToolSpecification.builder()
                        .name("executeSql")
                        .description("Execute SELECT SQL. Expects argument 'query'")
                        .build()
        );
    }

    public String run(String sessionId, String question) {

        // Load previous memory
        List<ChatMessage> messages =
                new ArrayList<>(memoryStore.get(sessionId));

        // If first turn → inject system rules
        if (messages.isEmpty()) {
            messages.add(SystemMessage.from("""
                        You are a SQLite agent connected to a real database.
                    
                        RULES:
                        - You MUST use tools for schema/data.
                        - NEVER guess.
                        - NEVER simulate.
                        - Use listTables / executeSql.
                        - End with:
                    
                        FINAL ANSWER: ...
                    """));
        }

        messages.add(UserMessage.from(question));
        log.info("[Session={}], Starting agent stream. \n Question : {} \n", sessionId, question);
        log.info("Memory store {}", memoryStore.get(sessionId));

        for (int step = 0; step < 8; step++) {

            log.info("[SESSION={}] 🔁 Step {}", sessionId, step + 1);
            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs())
                    .build();

            var response = model.chat(request);

            AiMessage ai = response.aiMessage();

            // save AI message
            memoryStore.append(sessionId, ai);
            messages.add(ai);

            /* ---------- FINAL ---------- */
            if (ai.text() != null
                    && ai.text().contains("FINAL ANSWER")
                    && messages.stream().anyMatch(m -> m instanceof ToolExecutionResultMessage)) {
                log.info("[SESSION={}] ✅ FINAL ANSWER reached", sessionId);
                return ai.text();
            }

            /* ---------- TOOL ---------- */
            var toolRequests = ai.toolExecutionRequests();

            if (toolRequests != null && !toolRequests.isEmpty()) {

                ToolExecutionRequest req = toolRequests.get(0);

                log.info("[SESSION={}] 🛠 Tool requested: {} | args={}",
                        sessionId, req.name(), req.arguments());

                Object result = dispatch(req);

                ToolExecutionResultMessage toolMsg =
                        ToolExecutionResultMessage.from(
                                req.name(),
                                req.id(),
                                String.valueOf(result)
                        );

                memoryStore.append(sessionId, toolMsg);
                messages.add(toolMsg);
                log.info("[SESSION={}] 📦 Tool result: {}",
                        sessionId, result);
                continue;
            }

            return ai.text();
        }

        return "FAILED: model did not terminate.";
    }

    /* ---------------- DISPATCH ---------------- */

    private Object dispatch(ToolExecutionRequest req) {

        Map<String, Object> args;

        try {
            args = mapper.readValue(req.arguments(), Map.class);
        } catch (Exception e) {
            return "Bad tool args: " + req.arguments();
        }

        return switch (req.name()) {

            case "listTables" -> sqlTool.listTables("x");
            case "describeTable" -> sqlTool.describeTable(args.get("table").toString());
            case "executeSql" -> sqlTool.executeSql(args.get("query").toString());
            default -> "Unknown tool: " + req.name();
        };
    }
}
