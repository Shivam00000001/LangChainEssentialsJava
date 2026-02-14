package com.learning.langchain.lessons.l02_memory.web;

import com.learning.langchain.lessons.l02_memory.orchestrator.SqlAgentWithMemoryRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/l02/agent")
public class SqlAgentMemoryController {

    private final SqlAgentWithMemoryRunner runner;


    public SqlAgentMemoryController(SqlAgentWithMemoryRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q, @RequestParam(defaultValue = "default") String session) {
        System.out.println("User Question: \n" + q + "\nSession: " + session + "\n");
        String response =  runner.run(q, session);
        System.out.println(response + "\n\n");
        return response;
    }
}
