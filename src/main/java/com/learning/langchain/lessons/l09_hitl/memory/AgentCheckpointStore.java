package com.learning.langchain.lessons.l09_hitl.memory;

import dev.langchain4j.data.message.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentCheckpointStore {

    private final Map<String, List<ChatMessage>> checkpoints = new ConcurrentHashMap<>();

    public void save(String sessionId, List<ChatMessage> chatMessages) {
        checkpoints.put(sessionId, chatMessages);
    }

    public List<ChatMessage> load (String sessionId) {
        return checkpoints.get(sessionId);
    }

    public void clear(String sessionId) {
        checkpoints.remove(sessionId);
    }

}
