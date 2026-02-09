package com.learning.langchain.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Data
@ConfigurationProperties(prefix = "llm")
@Profile("langchain4j")
public class LlmProperties {

    private String provider;
    private String baseUrl;
    private String model;
    private double temperature;
    private int timeoutSeconds;

}
