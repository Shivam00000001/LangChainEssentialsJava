package com.learning.langchain.lessons.l09_hitl.context;

public record ExecutionContext(
        boolean sqlApproved
) {

    public static ExecutionContext initial() {
        return new ExecutionContext(false);
    }

    public ExecutionContext approveSql() {
        return new ExecutionContext(true);
    }
}
