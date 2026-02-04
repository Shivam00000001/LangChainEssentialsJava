package com.learning.langchain.web;

import com.learning.langchain.ai.service.SqlAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final SqlAgentService agentService;

    public AgentController(SqlAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/client")
    public String askClient(@RequestParam String question) {
        System.out.println("User Question: \n" + question + "\n");
        String response = agentService.ask(question);
        System.out.println(response + "\n\n");
        return response;
    }

}
