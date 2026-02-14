package com.learning.langchain.lessons.l03_streaming.web;

import com.learning.langchain.lessons.l03_streaming.orchestrator.SqlAgentStreamingRunner;
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


    public SqlAgentStreamingController(SqlAgentStreamingRunner runner) {
        this.runner = runner;
    }

    @GetMapping(value= "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream (@RequestParam String question) {

        SseEmitter emitter = new SseEmitter(120_000L);
        new Thread(() -> {
            try {
                runner.stream(question, emitter);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

}
