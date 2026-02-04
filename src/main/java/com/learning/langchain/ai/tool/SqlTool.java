package com.learning.langchain.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
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

    @Tool(
            name = "Sql Executor",
            description = "Execute a SQL query on the Chinook database"
    )
    public List<Map<String, Object>> executeSql(String query) {
        return jdbcTemplate.queryForList(query);
    }


    // NEW — list tables
    @Tool(
            name = "list_tables",
            description = "List all tables in the SQLite database"
    )
    public List<String> listTables() {
        return jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table'",
                String.class
        );
    }

    // NEW — describe table
    @Tool(
            name = "describe_table",
            description = "Describe columns of a given table"
    )
    public List<Map<String, Object>> describeTable(String table) {
        return jdbcTemplate.queryForList(
                "PRAGMA table_info(" + table + ")"
        );
    }

}
