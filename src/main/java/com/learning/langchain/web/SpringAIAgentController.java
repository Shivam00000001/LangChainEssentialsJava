package com.learning.langchain.web;

import com.learning.langchain.ai.service.SpringAISqlAgentService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
@Profile("spring-ai")
public class SpringAIAgentController {

    private final SpringAISqlAgentService agentService;

    public SpringAIAgentController(SpringAISqlAgentService agentService) {
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
