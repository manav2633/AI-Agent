package com.agentframework.multi_agent_reliability.dto;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public class AgentExecutionRequest {
    
    @NotNull(message = "Framework type is required")
    private AgentFrameworkType frameworkType;
    
    @NotBlank(message = "Task description is required")
    private String taskDescription;
    
    @NotBlank(message = "Task input is required")
    private String taskInput;
    
    private String expectedOutput;
    
    private Long timeoutMs = 300000L; // Default 5 minutes
    
    private Integer maxRetries = 3;
    
    private String benchmarkRunId;
    
    private Map<String, String> metadata;
    
    private Boolean async = true;
    
    // Constructors
    public AgentExecutionRequest() {}
    
    public AgentExecutionRequest(AgentFrameworkType frameworkType, String taskDescription, String taskInput) {
        this.frameworkType = frameworkType;
        this.taskDescription = taskDescription;
        this.taskInput = taskInput;
    }
    
    // Getters and Setters
    public AgentFrameworkType getFrameworkType() {
        return frameworkType;
    }
    
    public void setFrameworkType(AgentFrameworkType frameworkType) {
        this.frameworkType = frameworkType;
    }
    
    public String getTaskDescription() {
        return taskDescription;
    }
    
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
    
    public String getTaskInput() {
        return taskInput;
    }
    
    public void setTaskInput(String taskInput) {
        this.taskInput = taskInput;
    }
    
    public String getExpectedOutput() {
        return expectedOutput;
    }
    
    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
    
    public Long getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public String getBenchmarkRunId() {
        return benchmarkRunId;
    }
    
    public void setBenchmarkRunId(String benchmarkRunId) {
        this.benchmarkRunId = benchmarkRunId;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public Boolean getAsync() {
        return async;
    }
    
    public void setAsync(Boolean async) {
        this.async = async;
    }
    
    @Override
    public String toString() {
        return "AgentExecutionRequest{" +
                "frameworkType=" + frameworkType +
                ", taskDescription='" + taskDescription + '\'' +
                ", async=" + async +
                ", timeoutMs=" + timeoutMs +
                '}';
    }
}




