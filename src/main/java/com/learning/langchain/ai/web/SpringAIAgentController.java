package com.learning.langchain.ai.web;

import com.learning.langchain.ai.service.SpringAISqlAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log =
            LoggerFactory.getLogger(SpringAIAgentController.class);

    public SpringAIAgentController(SpringAISqlAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/client")
    public String askClient(@RequestParam String question) {
        log.info("User Question: \n{}\n", question);
        String response = agentService.ask(question);
        log.info("{}\n\n", response);
        return response;
    }

}
