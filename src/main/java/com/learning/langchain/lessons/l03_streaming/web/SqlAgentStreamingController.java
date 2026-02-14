package com.learning.langchain.lessons.l03_streaming.web;

import com.learning.langchain.lessons.l03_streaming.orchestrator.SqlAgentStreamingRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/l03/agent")
public class SqlAgentStreamingController {

    private final SqlAgentStreamingRunner runner;

    private static final Logger log =
            LoggerFactory.getLogger(SqlAgentStreamingController.class);

    public SqlAgentStreamingController(SqlAgentStreamingRunner runner) {
        this.runner = runner;
    }

    @GetMapping(value= "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream (@RequestParam(defaultValue = "default") String sessionId, @RequestParam String question) {

        SseEmitter emitter = new SseEmitter(120_000L);
        new Thread(() -> {
            try {
                log.info("[Session={}], Controller request received. \n Question : {} \n", sessionId, question);
                runner.stream(sessionId, question, emitter);
                log.info("{} answered for session {}", question, sessionId);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

}
