package com.learning.langchain.shared.memory;

import dev.langchain4j.data.message.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemoryStore {

    private final ConcurrentHashMap<String, List<ChatMessage>> store = new ConcurrentHashMap<>();

    public List<ChatMessage> get(String sessionId) {
        return store.computeIfAbsent(sessionId, id -> new ArrayList<>());
    }

    public void append (String sessionId, ChatMessage chatMessage) {
        get(sessionId).add(chatMessage);
    }

    public void appendAll(String sessionId, List<ChatMessage> chatMessages) {
        get(sessionId).addAll(chatMessages);
    }

    public void clear(String sessionId) {
        store.remove(sessionId);
    }
}
