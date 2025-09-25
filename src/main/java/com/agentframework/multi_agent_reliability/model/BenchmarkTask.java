package com.agentframework.multi_agent_reliability.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "benchmark_tasks")
public class BenchmarkTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @NotBlank
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @NotBlank
    @Column(name = "task_input", nullable = false, columnDefinition = "TEXT")
    private String taskInput;
    
    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "complexity", nullable = false)
    private TaskComplexity complexity;
    
    @Column(name = "timeout_ms")
    private Long timeoutMs = 300000L; // Default 5 minutes
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @OneToMany(mappedBy = "benchmarkTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BenchmarkRun> benchmarkRuns = new ArrayList<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    // Constructors
    public BenchmarkTask() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BenchmarkTask(String name, String description, String taskInput, TaskComplexity complexity) {
        this();
        this.name = name;
        this.description = description;
        this.taskInput = taskInput;
        this.complexity = complexity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public TaskComplexity getComplexity() {
        return complexity;
    }
    
    public void setComplexity(TaskComplexity complexity2) {
        this.complexity = complexity2;
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
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public List<BenchmarkRun> getBenchmarkRuns() {
        return benchmarkRuns;
    }
    
    public void setBenchmarkRuns(List<BenchmarkRun> benchmarkRuns) {
        this.benchmarkRuns = benchmarkRuns;
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "BenchmarkTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", complexity=" + complexity +
                ", active=" + active +
                '}';
    }

    public enum TaskComplexity {
        SIMPLE("Simple", "Basic single-step tasks"),
        MODERATE("Moderate", "Multi-step tasks with some reasoning"),
        COMPLEX("Complex", "Advanced reasoning and multi-agent coordination"),
        EXPERT("Expert", "Highly complex domain-specific tasks");
        
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
}

