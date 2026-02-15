package com.learning.langchain.shared.playground;

import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("langchain4j")
public class LllmService {

    private final ChatModel chatLanguageModel;

    private static final Logger log =
            LoggerFactory.getLogger(LllmService.class);


    public LllmService(ChatModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public String ping() {

        log.info("LLM works");
        return chatLanguageModel.chat("Yes LLM works!!");
    }
}
