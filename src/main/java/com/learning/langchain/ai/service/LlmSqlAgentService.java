package com.learning.langchain.ai.service;


import com.learning.langchain.ai.agent.SqlAgent;
import com.learning.langchain.ai.tool.SqlTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

@Service
public class LlmSqlAgentService {

    private final SqlAgent sqlAgent;

    public LlmSqlAgentService(ChatLanguageModel chatLanguageModel, SqlTool sqlTool) {
        this.sqlAgent = AiServices.builder(SqlAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(sqlTool)
                .build();
    }

    public String ask(String question) {
        return sqlAgent.ask(question);
    }

}

