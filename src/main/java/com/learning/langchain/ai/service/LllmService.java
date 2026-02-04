package com.learning.langchain.ai.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class LllmService {

    private final ChatLanguageModel chatLanguageModel;


    public LllmService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public String ping() {
        return chatLanguageModel.generate("Yes LLM works!!");
    }
}
