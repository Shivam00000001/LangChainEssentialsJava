package com.learning.langchain.lessons.l02_memory.memory;

import dev.langchain4j.data.message.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryConversationStore {

    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();

    public List<ChatMessage> get(String sessionId) {
        return sessions.computeIfAbsent(
                sessionId, id -> new ArrayList<>()
        );
    }

    public void append (String sessionId, ChatMessage message) {
        get(sessionId).add(message);
    }

    public void appendAll(String sessionId, List<ChatMessage> messages) {
        get(sessionId).addAll(messages);
    }

    public void clear (String sessionId) {
        sessions.remove(sessionId);
    }

}
