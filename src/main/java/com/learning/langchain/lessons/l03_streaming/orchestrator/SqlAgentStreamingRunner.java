package com.learning.langchain.lessons.l03_streaming.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SqlAgentStreamingRunner {

    private final ChatModel model;
    private final SqlTool sqlTool;
    private final ObjectMapper mapper = new ObjectMapper();


    public SqlAgentStreamingRunner(ChatModel model, SqlTool sqlTool) {
        this.model = model;
        this.sqlTool = sqlTool;
    }

    private List<ToolSpecification> toolSpecs() {
        return List.of(
                ToolSpecification.builder()
                        .name("List Tables")
                        .description("List all the tables in the SQLite database")
                        .build(),
                ToolSpecification.builder()
                        .name("describeTable")
                        .description("Describe columns of the table, Expects 'table'")
                        .build(),
                ToolSpecification.builder()
                        .name("executeSql")
                        .description("Execute Select SQL, Expects 'query'")
                        .build()
        );
    }

    public void stream(String question, SseEmitter emitter) {

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from("""
                    You are a SQL agent connected to a real database.
                    
                    RULES:
                    - Use tools when needed
                    - Never guess or simulate
                    - END with a FINAl ANSWER
                """));
        messages.add(UserMessage.from(question));

        for (int step = 0; step < 8; step++) {

            send(emitter, "step", "LLM step " + step + 1);

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs())
                    .build();

            var response = model.chat(chatRequest);
            AiMessage ai = response.aiMessage();

            messages.add(ai);

            if (ai.text() != null) {
                send(emitter, "llm", ai.text());
            }

            if (ai.text() != null && ai.text().contains("FINAL ANSWER")) {
                send(emitter, "done", "completed");
                return;
            }

            var toolCalls = ai.toolExecutionRequests();

            if (toolCalls != null && !toolCalls.isEmpty()) {

                ToolExecutionRequest toolExecutionRequest = toolCalls.get(0);
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
