package com.learning.langchain.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SqlAgent {

    @SystemMessage("""
                You are a SQLite analyst working on the Chinook database.
                
                Rules:
                - Think step-by-step.
                - Inspect schema first using listTables or describeTable.
                - use executeSql to get data access.
                - Read only queries.
                - Prefer explicit columns.
                - Always provide a final answer.
                """)
    @UserMessage("{{it}}")
    String ask(String question);
}