package com.learning.langchain.lessons.l01_sql_agent.service;


import com.learning.langchain.ai.agent.SqlAgent;
import com.learning.langchain.lessons.l01_sql_agent.tool.SqlTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("langchain4j")
public class LlmSqlAgentService {

    private final SqlAgent sqlAgent;
    private static final Logger log =
            LoggerFactory.getLogger(LlmSqlAgentService.class);

    public LlmSqlAgentService(ChatModel chatLanguageModel, SqlTool sqlTool) {
        this.sqlAgent = AiServices.builder(SqlAgent.class)
                .chatModel(chatLanguageModel)
                .tools(sqlTool)
                .build();
    }

    public String ask(String question) {
        log.info("Agent called ");
        return sqlAgent.ask(question);
    }

}

