package com.learning.langchain.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAISqlAgentService {

    private final ChatClient chatClient;


    public SpringAISqlAgentService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String ask (String question ) {
        return chatClient.prompt()
                .system("""
                    You are a careful SQLite analyst working on the Chinook music database.
                        
                          Rules:
                        
                          - Think step-by-step.
                          - Before writing any query, discover the schema:
                            • call list_tables first.
                            • call describe_table(table_name) for any table you plan to use.
                        
                          - When you need data, call the tool `execute_sql` with ONE SELECT query only.
                          - Read-only only; no INSERT/UPDATE/DELETE/ALTER/DROP/CREATE/REPLACE/TRUNCATE.
                          - Prefer explicit column lists; avoid SELECT *.
                          - Add LIMIT 5 unless the user explicitly asks for more.
                          - If the tool returns "Error:", revise the SQL and try again.
                          - Always produce a complete final answer after tool calls.
                          - Never stop mid-sentence.
                        
                          Goal:
        
                          Answer the user's question correctly using database evidence.
                  """)
                .user(question)
                .call()
                .content();
    }
}
