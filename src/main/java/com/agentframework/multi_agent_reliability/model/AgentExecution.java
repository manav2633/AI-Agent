package com.agentframework.multi_agent_reliability.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "agent_executions")
public class AgentExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "framework_type", nullable = false)
    private AgentFrameworkType frameworkType;
    
    @NotBlank
    @Column(name = "task_description", nullable = false, length = 1000)
    private String taskDescription;
    
    @Column(name = "task_input", columnDefinition = "TEXT")
    private String taskInput;
    
    @Column(name = "task_output", columnDefinition = "TEXT")
    private String taskOutput;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
    @ElementCollection
    @CollectionTable(name = "execution_metadata", joinColumns = @JoinColumn(name = "execution_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    @Column(name = "benchmark_run_id")
    private String benchmarkRunId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public AgentExecution() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ExecutionStatus.PENDING;
    }
    
    public AgentExecution(AgentFrameworkType frameworkType, String taskDescription) {
        this();
        this.frameworkType = frameworkType;
        this.taskDescription = taskDescription;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getTaskOutput() {
        return taskOutput;
    }
    
    public void setTaskOutput(String taskOutput) {
        this.taskOutput = taskOutput;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (this.startTime != null && endTime != null) {
            this.executionDurationMs = java.time.Duration.between(this.startTime, endTime).toMillis();
        }
    }
    
    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }
    
    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getBenchmarkRunId() {
        return benchmarkRunId;
    }
    
    public void setBenchmarkRunId(String benchmarkRunId) {
        this.benchmarkRunId = benchmarkRunId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public void markAsStarted() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted(String output) {
        this.status = ExecutionStatus.COMPLETED;
        this.taskOutput = output;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateDuration();
    }
    
    private void calculateDuration() {
        if (this.startTime != null && this.endTime != null) {
            this.executionDurationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    @Override
    public String toString() {
        return "AgentExecution{" +
                "id=" + id +
                ", frameworkType=" + frameworkType +
                ", status=" + status +
                ", duration=" + executionDurationMs + "ms" +
                '}';
    }
}