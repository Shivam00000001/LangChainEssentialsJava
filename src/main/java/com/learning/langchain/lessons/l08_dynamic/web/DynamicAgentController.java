package com.learning.langchain.lessons.l08_dynamic.web;

import com.learning.langchain.lessons.l08_dynamic.orchestrator.DynamicSqlAgentRunner;
import com.learning.langchain.shared.policy.AgentPolicy;
import com.learning.langchain.shared.output.TableStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/l08/agent")
public class DynamicAgentController {

    private final DynamicSqlAgentRunner runner;


    private static final Logger log =
            LoggerFactory.getLogger(DynamicAgentController.class);


    public DynamicAgentController(DynamicSqlAgentRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/dynamic/readonly")
    public TableStats readOnly (@RequestParam String question) {
        log.info("Received question: {}", question);
        TableStats response = runner.run(question, AgentPolicy.readOnly());
        log.info("Response : \n{}", response);
        return response;
    }

    @GetMapping("/dynamic/full")
    public TableStats full (@RequestParam String question) {
        log.info("Received question: {}", question);
        TableStats response = runner.run(question, AgentPolicy.fullAccess());
        log.info("Response : \n{}", response);
        return response;
    }

}
