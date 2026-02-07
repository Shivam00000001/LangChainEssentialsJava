package com.learning.langchain.ai.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("langchain4j")
public class LllmService {

    private final ChatModel chatLanguageModel;


    public LllmService(ChatModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public String ping() {
        return chatLanguageModel.chat("Yes LLM works!!");
    }
}
