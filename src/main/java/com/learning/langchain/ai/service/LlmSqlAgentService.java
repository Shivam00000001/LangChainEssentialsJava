package com.learning.langchain.ai.service;


import com.learning.langchain.ai.agent.SqlAgent;
import com.learning.langchain.ai.tool.SqlTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("langchain4j")
public class LlmSqlAgentService {

    private final SqlAgent sqlAgent;

    public LlmSqlAgentService(ChatModel chatLanguageModel, SqlTool sqlTool) {
        this.sqlAgent = AiServices.builder(SqlAgent.class)
                .chatModel(chatLanguageModel)
                .tools(sqlTool)
                .build();
    }

    public String ask(String question) {
        return sqlAgent.ask(question);
    }

}

