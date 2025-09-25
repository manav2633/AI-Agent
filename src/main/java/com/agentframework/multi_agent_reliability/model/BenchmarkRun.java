package com.agentframework.multi_agent_reliability.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "benchmark_runs")
public class BenchmarkRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "run_id", unique = true, nullable = false)
    private String runId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benchmark_task_id", nullable = false)
    private BenchmarkTask benchmarkTask;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "benchmark_run_id", referencedColumnName = "run_id")
    private List<AgentExecution> executions = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BenchmarkRunStatus status = BenchmarkRunStatus.PENDING;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "total_executions")
    private Integer totalExecutions = 0;
    
    @Column(name = "completed_executions")
    private Integer completedExecutions = 0;
    
    @Column(name = "failed_executions")
    private Integer failedExecutions = 0;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    // Constructors
    public BenchmarkRun() {
        this.createdAt = LocalDateTime.now();
        this.runId = generateRunId();
    }
    
    public BenchmarkRun(BenchmarkTask benchmarkTask, String name) {
        this();
        this.benchmarkTask = benchmarkTask;
        this.name = name;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public BenchmarkTask getBenchmarkTask() {
        return benchmarkTask;
    }
    
    public void setBenchmarkTask(BenchmarkTask benchmarkTask) {
        this.benchmarkTask = benchmarkTask;
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
    
    public List<AgentExecution> getExecutions() {
        return executions;
    }
    
    public void setExecutions(List<AgentExecution> executions) {
        this.executions = executions;
    }
    
    public BenchmarkRunStatus getStatus() {
        return status;
    }
    
    public void setStatus(BenchmarkRunStatus status) {
        this.status = status;
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
    }
    
    public Integer getTotalExecutions() {
        return totalExecutions;
    }
    
    public void setTotalExecutions(Integer totalExecutions) {
        this.totalExecutions = totalExecutions;
    }
    
    public Integer getCompletedExecutions() {
        return completedExecutions;
    }
    
    public void setCompletedExecutions(Integer completedExecutions) {
        this.completedExecutions = completedExecutions;
    }
    
    public Integer getFailedExecutions() {
        return failedExecutions;
    }
    
    public void setFailedExecutions(Integer failedExecutions) {
        this.failedExecutions = failedExecutions;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public enum BenchmarkRunStatus {
        PENDING("Pending", "Benchmark run is queued"),
        RUNNING("Running", "Benchmark run is in progress"),
        COMPLETED("Completed", "Benchmark run completed successfully"),
        FAILED("Failed", "Benchmark run failed"),
        CANCELLED("Cancelled", "Benchmark run was cancelled");
        
        private final String displayName;
        private final String description;
        
        BenchmarkRunStatus(String displayName, String description) {
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
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }
    }
    
    // Utility methods
    public void addExecution(AgentExecution execution) {
        this.executions.add(execution);
        execution.setBenchmarkRunId(this.runId);
        this.totalExecutions = this.executions.size();
    }
    
    public void updateStats() {
        this.completedExecutions = (int) executions.stream()
                .mapToInt(e -> e.getStatus() == ExecutionStatus.COMPLETED ? 1 : 0)
                .sum();
        this.failedExecutions = (int) executions.stream()
                .mapToInt(e -> e.getStatus() == ExecutionStatus.FAILED ? 1 : 0)
                .sum();
    }
    
    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) completedExecutions / totalExecutions * 100 : 0.0;
    }
    
    private String generateRunId() {
        return "RUN_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @Override
    public String toString() {
        return "BenchmarkRun{" +
                "id=" + id +
                ", runId='" + runId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", totalExecutions=" + totalExecutions +
                '}';
    }
}

