package com.agentframework.multi_agent_reliability.dto;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;
import java.util.Map;

public class BenchmarkRequest {
    
    @NotBlank(message = "Benchmark name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotNull(message = "Framework types are required")
    private List<AgentFrameworkType> frameworkTypes;
    
    @Min(value = 1, message = "Iterations must be at least 1")
    @Max(value = 100, message = "Iterations cannot exceed 100")
    private Integer iterations = 5;
    
    private Long timeoutMs = 300000L; // Default 5 minutes
    
    private Integer maxRetries = 3;
    
    private Boolean parallel = false;
    
    private Map<String, String> metadata;
    
    private String createdBy;
    
    // Constructors
    public BenchmarkRequest() {}
    
    public BenchmarkRequest(String name, Long taskId, List<AgentFrameworkType> frameworkTypes) {
        this.name = name;
        this.taskId = taskId;
        this.frameworkTypes = frameworkTypes;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public List<AgentFrameworkType> getFrameworkTypes() {
        return frameworkTypes;
    }
    
    public void setFrameworkTypes(List<AgentFrameworkType> frameworkTypes) {
        this.frameworkTypes = frameworkTypes;
    }
    
    public Integer getIterations() {
        return iterations;
    }
    
    public void setIterations(Integer iterations) {
        this.iterations = iterations;
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
    
    public Boolean getParallel() {
        return parallel;
    }
    
    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    // Utility methods
    public int getTotalExecutions() {
        return frameworkTypes != null ? frameworkTypes.size() * iterations : 0;
    }
    
    public boolean hasMultipleFrameworks() {
        return frameworkTypes != null && frameworkTypes.size() > 1;
    }
    
    @Override
    public String toString() {
        return "BenchmarkRequest{" +
                "name='" + name + '\'' +
                ", taskId=" + taskId +
                ", frameworkTypes=" + frameworkTypes +
                ", iterations=" + iterations +
                ", parallel=" + parallel +
                '}';
    }
}