package com.learning.langchain.lessons.l08_dynamic.policy;

public record AgentPolicy(
        boolean allowSqlExecution,
        boolean allowSchemaInspection
) {

    public static AgentPolicy readOnly() {
        return new AgentPolicy(false, true);
    }

    public static AgentPolicy fullAccess() {
        return new AgentPolicy(true, true);
    }

}
