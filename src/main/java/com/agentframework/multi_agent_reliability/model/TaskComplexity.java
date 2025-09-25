package com.agentframework.multi_agent_reliability.model;

/**
 * Enumeration of task complexity levels for benchmarking.
 */
public enum TaskComplexity {
    SIMPLE("Simple", "Basic single-step tasks"),
    MODERATE("Moderate", "Multi-step tasks with moderate reasoning"),
    COMPLEX("Complex", "Advanced tasks requiring multi-agent coordination"),
    EXPERT("Expert", "Highly domain-specific or multi-stage tasks");

    private final String displayName;
    private final String description;

    TaskComplexity(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
