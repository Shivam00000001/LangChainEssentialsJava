package com.learning.langchain.lessons.l02_memory.web;

import com.learning.langchain.lessons.l02_memory.orchestrator.SqlAgentWithMemoryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/l02/agent")
public class SqlAgentMemoryController {

    private final SqlAgentWithMemoryRunner runner;

    private static final Logger log =
            LoggerFactory.getLogger(SqlAgentMemoryController.class);


    public SqlAgentMemoryController(SqlAgentWithMemoryRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q, @RequestParam(defaultValue = "default") String session) {
        log.info("User Question: \n{}\nSession: {}\n", q, session);
        String response =  runner.run(q, session);
        log.info("{}\n\n", response);
        return response;
    }
}
