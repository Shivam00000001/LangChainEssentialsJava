package com.learning.langchain.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class OllamaLangChain4jConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(LlmProperties props) {
        return OllamaChatModel.builder()
                .baseUrl(props.getBaseUrl())
                .modelName(props.getModel())
                .temperature(props.getTemperature())
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .build();
    }
}
