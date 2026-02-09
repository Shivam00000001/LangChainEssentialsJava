package com.learning.langchain.lessons.l01_sql_agent.orchestrator;

import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.ChatRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class SqlAgentRunner {

    private final ChatModel model;
    private final SqlTool sqlTool;


    public SqlAgentRunner(ChatModel model, SqlTool sqlTool) {
        this.model = model;
        this.sqlTool = sqlTool;
    }


    private List<ToolSpecification> toolSpecs() {

        return List.of(
                ToolSpecification.builder()
                        .name("listTables")
                        .description("List all tables in the SQLite database")
                        .build(),

                ToolSpecification.builder()
                        .name("describeTable")
                        .description("Describe columns of a table. Expects an argument 'table'.")
                        .build(),

                ToolSpecification.builder()
                        .name("executeSql")
                        .description("Execute a SELECT SQL query. Expects an argument 'query'.")
                        .build()
        );
    }


    public String run(String question) {

        List<ChatMessage> messages = new ArrayList<>();

        messages.add(SystemMessage.from("""
            You are a SQLite agent connected to a real database.
            
            CRITICAL RULES:
            - You do NOT know the schema in advance.
            - You MUST use tools to answer.
            - For any schema or data question you MUST call a tool.
            - NEVER guess table names.
            - NEVER simulate results.
            - If you have not called a tool, you are not allowed to answer.
            
            For listing tables:
            - Call the tool listTables.
            
            For counting rows:
            - Call executeSql.
            
            After finishing, respond:
            
            FINAL ANSWER: ...
            
            If you violate these rules, the answer is invalid.
        """));


        messages.add(UserMessage.from(question));
        System.out.println(messages);
        for (int step = 0; step < 8; step++) {
            System.out.println("Step " + (step + 1) + ": ");

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs())
                    .build();

            var response = model.chat(request);
            System.out.println(response.aiMessage());

            AiMessage ai = response.aiMessage();

            // ---- Final answer ----
            if (ai.text() != null
                    && ai.text().contains("FINAL ANSWER")
                    && messages.stream().anyMatch(m -> m instanceof ToolExecutionResultMessage)) {

                return ai.text();
            }


            // ---- Tool call ----
            var toolRequests = ai.toolExecutionRequests();

            if (toolRequests != null && !toolRequests.isEmpty()) {

                ToolExecutionRequest req = toolRequests.get(0);

                Object toolResult = dispatch(req);

                messages.add(ai);

                messages.add(ToolExecutionResultMessage.from(
                        req.name(),
                        req.id(),
                        String.valueOf(toolResult)
                ));
                continue;
        }

            // fallback
            return ai.text();
        }

        return "FAILED: model did not terminate in time.";
    }

    private Object dispatch(ToolExecutionRequest req) {

        String json = req.arguments();   // <-- raw JSON string

        Map<String, Object> args;

        try {
            args = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, Map.class);
        } catch (Exception e) {
            return "Failed to parse tool arguments: " + json;
        }

        return switch (req.name()) {
            case "listTables" -> sqlTool.listTables("x");
            case "describeTable" -> sqlTool.describeTable(
                    args.get("table").toString()
            );
            case "executeSql" -> sqlTool.executeSql(
                    args.get("query").toString()
            );
            default -> "Unknown tool: " + req.name();
        };
    }

}
