package com.learning.langchain.lessons.l09_hitl.web;

import com.learning.langchain.lessons.l09_hitl.context.ExecutionContext;
import com.learning.langchain.shared.policy.AgentPolicy;
import com.learning.langchain.lessons.l09_hitl.orchestrator.HitlSqlAgentRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/l09/agent")
public class HitlAgentController {

    private final HitlSqlAgentRunner runner;


    private static final Logger log =
            LoggerFactory.getLogger(HitlAgentController.class);


    public HitlAgentController(HitlSqlAgentRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/ask")
    public Object ask(@RequestParam(defaultValue = "default") String sessionId,
                      @RequestParam String question) {

        log.info("Request received with {} and {}", sessionId, question);
        Object ob = runner.run(sessionId, question, AgentPolicy.fullAccess(), ExecutionContext.initial());
        log.info("Response : {}", ob);
        return ob;
    }

    @GetMapping("/resume")
    public Object resume(@RequestParam(defaultValue = "default") String sessionId,
                         @RequestParam String decision) {


        log.info("Request received with {} and {}", sessionId, decision);

        ExecutionContext context =
                "Approve".equalsIgnoreCase(decision)
                        ? ExecutionContext.initial().approveSql()
                        : ExecutionContext.initial();

        log.info("Context is {}", context);

        Object ob = runner.run(
                sessionId,
                "Human approved SQL execution",
                AgentPolicy.fullAccess(),
                context
        );

        log.info("Response : {}", ob);
        return ob;
    }

}
