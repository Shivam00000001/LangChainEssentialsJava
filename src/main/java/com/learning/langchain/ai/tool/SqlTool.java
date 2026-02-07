package com.learning.langchain.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SqlTool {

    private final JdbcTemplate jdbcTemplate;


    public SqlTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool("Execute a SQL query on the Chinook database")
    public List<Map<String, Object>> executeSql(String query) {
        return jdbcTemplate.queryForList(query);
    }


    // NEW — list tables
     @Tool("Use this to list ALL tables in the SQLite database. Required before answering any schema question.")
    public List<String> listTables(String input) {
        return jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table'",
                String.class
        );
    }

    // NEW — describe table
    @Tool("Describe columns of a given table")
    public List<Map<String, Object>> describeTable(String table) {
        return jdbcTemplate.queryForList(
                "PRAGMA table_info(" + table + ")"
        );
    }

}
