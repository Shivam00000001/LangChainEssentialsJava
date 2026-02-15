package com.learning.langchain.lessons.l08_dynamic.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import com.learning.langchain.lessons.l08_dynamic.policy.AgentPolicy;
import com.learning.langchain.shared.output.TableStats;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DynamicSqlAgentRunner {

    private final ChatModel chatModel;
    private final SqlTool sqlTool;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger log =
            LoggerFactory.getLogger(DynamicSqlAgentRunner.class);


    public DynamicSqlAgentRunner(ChatModel chatModel, SqlTool sqlTool) {
        this.chatModel = chatModel;
        this.sqlTool = sqlTool;
    }

    private List<ToolSpecification> selectTools (AgentPolicy policy) {
        List<ToolSpecification> tools = new ArrayList<>();

        if (policy.allowSchemaInspection()) {
            log.info("Allowing schema inspection");
            tools.add(ToolSpecification.builder()
                    .name("listTables")
                    .description("List all the tables in the SQLite database")
                    .parameters(JsonObjectSchema.builder().build())
                    .build()
            );
        }

        if (policy.allowSqlExecution()) {
            log.info("Allowing SQL execution");
            tools.add(ToolSpecification.builder()
                    .name("executeSql")
                    .description("Execute a select SQL query")
                    .parameters(
                            JsonObjectSchema.builder()
                                    .addProperty("query", JsonStringSchema.builder().build())
                                    .required("query")
                                    .build()
                    )
                    .build()
            );
        }

        return tools;
    }

    private SystemMessage systemPrompt (AgentPolicy policy) {

        if(!policy.allowSqlExecution()) {
            return SystemMessage.from("""
                    You are a database analyst.
                    You may inspect schema but MUST NOT execute SQL queries.
                    Never guess data.
                    """);
        }

        return SystemMessage.from("""
            You are a SQL agent connected to a real database.
            Use tools when required.
            Never guess or simulate.
            Return final answer as JSON:
            {
              "table": string,
              "rowCount": number
            }
        """);

    }

    public TableStats run (String question, AgentPolicy policy) {

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemPrompt(policy));
        messages.add(UserMessage.from(question));

        log.info("Messages : \n {}", messages);

        for (int step = 0; step < 6; step++) {

            log.info("Step {} of agent execution", (step + 1));

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(selectTools(policy))
                    .build();
            var response = chatModel.chat(chatRequest);
            AiMessage ai = response.aiMessage();
            messages.add(ai);
            log.info("AI response : \n {}", ai);

            if (ai.text() != null) {
                try {
                    return mapper.readValue(ai.text(), TableStats.class);
                }catch (Exception ignored) {
                    // Not a final answer
                }
            }

            // Tool handling

            var toolCalls = ai.toolExecutionRequests();
            if (toolCalls != null && !toolCalls.isEmpty()) {
                ToolExecutionRequest req = toolCalls.get(0);

                log.info("Tool Requested : \n{}{}{}", req.name(), req.arguments(), req.id());

                Object result = dispatch(req, policy);
                messages.add(
                        ToolExecutionResultMessage.from(
                                req.name(),
                                req.id(),
                                String.valueOf(result)
                        )
                );
                log.info("Tool Result :{}", result);
            }

        }
        throw new IllegalStateException("Agent didn't converge");
    }

    private Object dispatch(ToolExecutionRequest req, AgentPolicy policy) {

        Map<String, Object> args;
        try {
            args = mapper.readValue(req.arguments(), Map.class);
        } catch (Exception e) {
            return "Invalid tool arguments";
        }

        return switch (req.name()) {

            case "listTables" -> {
                if (!policy.allowSchemaInspection()) {
                    yield "Schema inspection not allowed";
                }
                log.info("Executing listTables request");
                yield sqlTool.listTables("x");
            }

            case "executeSql" -> {
                if (!policy.allowSqlExecution()) {
                    yield "SQL execution not allowed";
                }
                log.info("Executing executeSql request with query: {}", args.get("query"));
                yield sqlTool.executeSql(args.get("query").toString());
            }

            default -> "Unknown tool";
        };
    }

}
