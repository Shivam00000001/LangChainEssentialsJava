package com.learning.langchain.web;

import com.learning.langchain.ai.service.LlmSqlAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm-agent")
public class LlmAgentController {

    private final LlmSqlAgentService sqlAgentService;


    public LlmAgentController(LlmSqlAgentService sqlAgentService) {
        this.sqlAgentService = sqlAgentService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        System.out.println("User Question: \n" + question + "\n");
        String response =  sqlAgentService.ask(question);
        System.out.println(response + "\n\n");
        return response;
    }
}
