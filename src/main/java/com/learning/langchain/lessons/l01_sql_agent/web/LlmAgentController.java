package com.learning.langchain.lessons.l01_sql_agent.web;

import com.learning.langchain.lessons.l01_sql_agent.orchestrator.SqlAgentRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm-agent")
@Profile("langchain4j")
public class LlmAgentController {

    private  final SqlAgentRunner agentRunner;

    private static final Logger log =
            LoggerFactory.getLogger(LlmAgentController.class);


    public LlmAgentController(SqlAgentRunner agentRunner) {
        this.agentRunner = agentRunner;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        log.info("User Question: \n{}\n", question);
        String response =  agentRunner.run(question);
        log.info("{}\n\n", response);
        return response;
    }

}
