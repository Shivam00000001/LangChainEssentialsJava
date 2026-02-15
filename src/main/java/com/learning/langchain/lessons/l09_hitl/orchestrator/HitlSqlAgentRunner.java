package com.learning.langchain.lessons.l09_hitl.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import com.learning.langchain.lessons.l09_hitl.context.ExecutionContext;
import com.learning.langchain.shared.policy.AgentPolicy;
import com.learning.langchain.lessons.l09_hitl.memory.AgentCheckpointStore;
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
public class HitlSqlAgentRunner {

    private final ChatModel model;
    private final SqlTool sqlTool;
    private final AgentCheckpointStore checkpointStore;
    private final ObjectMapper mapper = new ObjectMapper();


    private static final Logger log =
            LoggerFactory.getLogger(HitlSqlAgentRunner.class);

    public HitlSqlAgentRunner(ChatModel model,
                              SqlTool sqlTool,
                              AgentCheckpointStore checkpointStore) {
        this.model = model;
        this.sqlTool = sqlTool;
        this.checkpointStore = checkpointStore;
    }

    /* =====================
       ENTRY POINT
       ===================== */

    public Object run(String sessionId,
                      String question,
                      AgentPolicy policy,
                      ExecutionContext context) {

        List<ChatMessage> messages =
                checkpointStore.load(sessionId);


        if (messages == null) {
            log.info("Checkpoint not found, starting new session");
            messages = new ArrayList<>();
            messages.add(systemPrompt(policy));
            messages.add(UserMessage.from(question));
        }

        for (int step = 0; step < 8; step++) {


            log.info("Step {} of agent execution", (step + 1));

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(selectTools(policy))
                    .build();

            AiMessage ai = model.chat(request).aiMessage();
            messages.add(ai);
            log.info("AI response : \n {}", ai);

            // ---------- Structured Output ----------
            if (ai.text() != null) {
                try {
                    TableStats stats =
                            mapper.readValue(ai.text(), TableStats.class);

                    log.info("✅ Final structured answer captured: {}", stats);

                    // IMPORTANT: stop tool usage immediately
                    checkpointStore.clear(sessionId);
                    return stats;

                } catch (Exception ignored) {
                    // not valid JSON yet
                    log.info("Mapper error {}", String.valueOf(ignored));
                }
            }


            // ---------- Tool Handling ----------
            var toolCalls = ai.toolExecutionRequests();
            if (toolCalls != null && !toolCalls.isEmpty()) {

                ToolExecutionRequest req = toolCalls.get(0);

                // 🔴 HITL PAUSE POINT
                if (requiresApproval(req, policy, context)) {
                    checkpointStore.save(sessionId, messages);
                    log.info("Requesting for user approval with {}{}{}", req.name(), req.id(), req.arguments());
                    return "HITL_REQUIRED: Approve SQL execution";
                }

                Object result = dispatch(req, policy);

                messages.add(
                        ToolExecutionResultMessage.from(
                                req.name(),
                                req.id(),
                                String.valueOf(result)
                        )
                );

            }

        }

        throw new IllegalStateException("Agent did not converge");
    }

    /* =====================
       HITL RULE
       ===================== */

    private boolean requiresApproval(
            ToolExecutionRequest req,
            AgentPolicy policy,
            ExecutionContext context
    ) {
        return "executeSql".equals(req.name())
                && req.arguments().toLowerCase().contains("count")
                && policy.allowSqlExecution()
                && !context.sqlApproved();
    }


    /* =====================
       PROMPT & TOOLS
       ===================== */

    private SystemMessage systemPrompt(AgentPolicy policy) {
        return SystemMessage.from("""
            You are a SQL agent connected to a REAL SQLite database.
        
            CRITICAL DATABASE RULES:
            - The database is SQLite (NOT MySQL, NOT PostgreSQL).
            - Do NOT use information_schema.
            - Do NOT use DATABASE().
            - For listing tables, use:
              SELECT name FROM sqlite_master WHERE type='table'
            - For table metadata, use:
              PRAGMA table_info(<table_name>)
        
            GENERAL RULES:
                    - Use tools when needed.
                    - Never guess or simulate results.
                    - If a SQL query fails, revise it and try again.
                
            TERMINATION RULE:
                    - You may use SQL tools multiple times to explore or correct.
                    - Once you have enough information to answer, you MUST stop calling tools
                             and return the FINAL JSON answer.
                    - Do NOT repeat the same query endlessly.
                    - If a query fails or is not useful, revise and try a different one.
                       
        
            OUTPUT FORMAT:
            - You MUST return the final answer as valid JSON ONLY.
            - Do not include explanations, markdown, or extra text.
        
            JSON schema:
            {
              "table": string,
              "rowCount": number
            }
        """);

    }

    private List<ToolSpecification> selectTools(AgentPolicy policy) {

        List<ToolSpecification> tools = new ArrayList<>();

        tools.add(
                ToolSpecification.builder()
                        .name("listTables")
                        .description("List tables")
                        .parameters(JsonObjectSchema.builder().build())
                        .build()
        );

        if (policy.allowSqlExecution()) {
            tools.add(
                    ToolSpecification.builder()
                            .name("executeSql")
                            .description("Execute SELECT SQL")
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

    /* =====================
       DISPATCH
       ===================== */

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
                    yield "POLICY_VIOLATION: Schema inspection is not allowed";
                }
                yield sqlTool.listTables("x");
            }

            case "executeSql" -> {
                if (!policy.allowSqlExecution()) {
                    yield "POLICY_VIOLATION: SQL execution is not allowed";
                }

                try {
                    yield sqlTool.executeSql(args.get("query").toString());
                } catch (Exception e) {
                    yield "SQL_ERROR: " + e.getMessage();
                }
            }

            default -> "Unknown tool: " + req.name();
        };
    }

}
