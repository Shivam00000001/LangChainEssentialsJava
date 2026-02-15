package com.learning.langchain.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SqlAgent {

    @SystemMessage("""
        You are a SQLite agent working on the Chinook database.
        
        You MUST do exactly this:
        
        1) Call listTables once.
        2) Identify candidate tables.
        3) Call executeSql with:
           SELECT COUNT(*) FROM <table>
           for each candidate.
        4) Decide which table has the maximum rows.
        5) Reply ONLY in this format:
        
        FINAL ANSWER: <table_name> (<row_count>)
        
        CRITICAL:
        - After you output FINAL ANSWER, STOP.
        - Do NOT call tools again.
        - Never repeat the same tool call.
        - If you already have row counts, finish.
        - Maximum 6 tool calls total.
    """)
    @UserMessage("{{it}}")
    String ask(String question);

}