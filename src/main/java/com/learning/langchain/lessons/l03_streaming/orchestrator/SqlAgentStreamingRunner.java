package com.learning.langchain.lessons.l03_streaming.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import com.learning.langchain.shared.memory.ConversationMemoryStore;
import com.learning.langchain.shared.output.TableStats;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SqlAgentStreamingRunner {

    private final ChatModel model;
    private final SqlTool sqlTool;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConversationMemoryStore memoryStore;

    private static final Logger log =
            LoggerFactory.getLogger(SqlAgentStreamingRunner.class);


    public SqlAgentStreamingRunner(ChatModel model, SqlTool sqlTool, ConversationMemoryStore memoryStore) {
        this.model = model;
        this.sqlTool = sqlTool;
        this.memoryStore = memoryStore;
    }

    private List<ToolSpecification> toolSpecs() {
        return List.of(
                ToolSpecification.builder()
                        .name("List Tables")
                        .description("List all the tables in the SQLite database")
                        .parameters(JsonObjectSchema.builder().build())
                        .build(),
                ToolSpecification.builder()
                        .name("describeTable")
                        .description("Describe columns of the table, Expects 'table'")
                        .parameters(JsonObjectSchema.builder()
                                .addProperty("table", JsonStringSchema.builder().build())
                                .required("table")
                                .build())
                        .build(),
                ToolSpecification.builder()
                        .name("executeSql")
                        .description("Execute Select SQL, Expects 'query'")
                        .parameters(JsonObjectSchema.builder()
                                .addProperty("query", JsonStringSchema.builder().build())
                                .required("query")
                                .build())
                        .build()
        );
    }

    public void stream(String sessionId, String question, SseEmitter emitter) {

        List<ChatMessage> messages = new ArrayList<>(memoryStore.get(sessionId));
        boolean toolUsed = false;

        messages.add(SystemMessage.from("""
                    You are a SQL agent connected to a real database.
                    
                    RULES:
                    - Use tools when needed
                    - Never guess or simulate
                    - You MUST return the final answer as valid JSON ONLY.
                      Do not include explanations, markdown, or extra text.
                
                      The JSON schema is:
                
                      {
                        "table": string,
                        "rowCount": number
                      }
                """));
        UserMessage userMessage = UserMessage.from(question);
        messages.add(userMessage);
        memoryStore.append(sessionId, userMessage);

        log.info("[Session={}], Starting agent stream. \n Question : {} \n", sessionId, question);
        log.info("Memory store {}", memoryStore.get(sessionId));


        for (int step = 0; step < 8; step++) {

            log.info("[SESSION={}] 🔁 Step {}", sessionId, step + 1);
            send(emitter, "step", "LLM step " + (step + 1));

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs())
                    .build();

            var response = model.chat(chatRequest);
            AiMessage ai = response.aiMessage();

            messages.add(ai);
            memoryStore.append(sessionId, ai);

            if (ai.text() != null) {
                log.info("[SESSION={}] 🤖 LLM: {}", sessionId, ai.text());
                send(emitter, "llm", ai.text());
            }

            if (ai.text() != null) {
                try {
                    // Attempt to parse structured output
                    TableStats stats =
                            mapper.readValue(ai.text(), TableStats.class);

                    if (!toolUsed) {
                        send(emitter, "error", "Model produced structured output without using tools");
                        return;
                    }

                    // Emit structured result
                    send(emitter, "result", String.valueOf(stats));

                    // Finish stream
                    log.info("[SESSION={}] ✅ FINAL ANSWER reached", sessionId);
                    send(emitter, "done", "completed");
                    return;

                } catch (Exception ignored) {
                    // Not final JSON yet → continue agent loop
                }
            }

            var toolCalls = ai.toolExecutionRequests();

            if (toolCalls != null && !toolCalls.isEmpty()) {
                ToolExecutionRequest toolExecutionRequest = toolCalls.get(0);
                toolUsed = true;

                log.info("[SESSION={}] 🛠 Tool requested: {} | args={}",
                        sessionId, toolExecutionRequest.name(), toolExecutionRequest.arguments());

                send(emitter, "tool-request", toolExecutionRequest.toString());

                Object result = dispatch(toolExecutionRequest);

                send(emitter, "tool-result", result.toString());

                ToolExecutionResultMessage toolMessage =
                    ToolExecutionResultMessage.from(
                            toolExecutionRequest.name(),
                            toolExecutionRequest.id(),
                            result.toString()
                    );

                messages.add(toolMessage);
                memoryStore.append(sessionId, toolMessage);
                log.info("[SESSION={}] 📦 Tool result: {}",
                        sessionId, result);
                continue;
            }
            return;
        }
        send(emitter, "error", "Agent didn't finish in time");

    }

    private void send (SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object dispatch (ToolExecutionRequest req) {
        Map<String, Object> args;

        try {
            args = mapper.readValue(req.arguments(), Map.class);
        } catch (Exception e) {
            return "Invalid tools args " + req.arguments();
        }

        return  switch (req.name()) {
            case "listTables" -> sqlTool.listTables("x");
            case "describeTable" -> sqlTool.describeTable(args.get("table").toString());
            case "executeSql" -> sqlTool.executeSql(args.get("query").toString());
            default -> "Unknown tool " + req.name();
        };
    }

}
