package com.agentframework.multi_agent_reliability.model;

public enum ExecutionStatus {
    PENDING("Pending", "Task is queued for execution"),
    RUNNING("Running", "Task is currently being executed"),
    COMPLETED("Completed", "Task completed successfully"),
    FAILED("Failed", "Task failed during execution"),
    TIMEOUT("Timeout", "Task exceeded maximum execution time"),
    CANCELLED("Cancelled", "Task was cancelled by user");
    
    private final String displayName;
    private final String description;
    
    ExecutionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }
    
    public boolean isSuccess() {
        return this == COMPLETED;
    }
}