package com.learning.langchain.web;

import com.learning.langchain.ai.service.LllmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm")
public class LlmController {

    private final LllmService lllmService;


    public LlmController(LllmService lllmService) {
        this.lllmService = lllmService;
    }

    @GetMapping("/ping")
    public String ping() {
        String response =  lllmService.ping();
        System.out.println("Response : \n" + response + "\n\n");
        return response;
    }

}
