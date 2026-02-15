package com.learning.langchain.shared.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm")
@Profile("langchain4j")
public class LlmController {

    private final LllmService lllmService;

    private static final Logger log =
            LoggerFactory.getLogger(LlmController.class);


    public LlmController(LllmService lllmService) {
        this.lllmService = lllmService;
    }

    @GetMapping("/ping")
    public String ping() {
        String response =  lllmService.ping();
        log.info("Response : \n{}\n\n", response);
        return response;
    }

}
