package com.learning.langchain.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String provider;
    private String baseUrl;
    private String model;
    private double temperature;
    private int timeoutSeconds;

}
