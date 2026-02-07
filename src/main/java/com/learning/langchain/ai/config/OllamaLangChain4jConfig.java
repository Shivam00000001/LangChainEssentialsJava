package com.learning.langchain.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
@Profile("langchain4j")
public class OllamaLangChain4jConfig {

    @Bean
    public ChatModel chatLanguageModel(LlmProperties props) {
        return OllamaChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .modelName(props.getModel())
                .temperature(props.getTemperature())
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .build();
    }
}
