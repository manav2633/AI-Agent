package com.agentframework.multi_agent_reliability.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "reliability_metrics")
public class ReliabilityMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "benchmark_run_id", nullable = false)
    private String benchmarkRunId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "framework_type", nullable = false)
    private AgentFrameworkType frameworkType;
    
    // Core Reliability Metrics
    @Column(name = "success_rate")
    private Double successRate = 0.0;
    
    @Column(name = "average_response_time_ms")
    private Double averageResponseTimeMs = 0.0;
    
    @Column(name = "median_response_time_ms")
    private Double medianResponseTimeMs = 0.0;
    
    @Column(name = "min_response_time_ms")
    private Long minResponseTimeMs = 0L;
    
    @Column(name = "max_response_time_ms")
    private Long maxResponseTimeMs = 0L;
    
    // Advanced Reliability Metrics
    @Column(name = "consistency_score")
    private Double consistencyScore = 0.0; // Variance in output quality
    
    @Column(name = "robustness_index")
    private Double robustnessIndex = 0.0; // Performance under stress
    
    @Column(name = "error_rate")
    private Double errorRate = 0.0;
    
    @Column(name = "timeout_rate")
    private Double timeoutRate = 0.0;
    
    @Column(name = "retry_rate")
    private Double retryRate = 0.0;
    
    // Resource Usage Metrics
    @Column(name = "average_memory_usage_mb")
    private Double averageMemoryUsageMb = 0.0;
    
    @Column(name = "peak_memory_usage_mb")
    private Double peakMemoryUsageMb = 0.0;
    
    @Column(name = "average_cpu_usage_percent")
    private Double averageCpuUsagePercent = 0.0;
    
    @Column(name = "peak_cpu_usage_percent")
    private Double peakCpuUsagePercent = 0.0;
    
    // Statistical Data
    @Column(name = "total_executions")
    private Integer totalExecutions = 0;
    
    @Column(name = "successful_executions")
    private Integer successfulExecutions = 0;
    
    @Column(name = "failed_executions")
    private Integer failedExecutions = 0;
    
    @Column(name = "timeout_executions")
    private Integer timeoutExecutions = 0;
    
    // Quality Metrics
    @Column(name = "output_quality_score")
    private Double outputQualityScore = 0.0; // Based on expected vs actual output comparison
    
    @Column(name = "response_relevance_score")
    private Double responseRelevanceScore = 0.0;
    
    @Column(name = "response_completeness_score")
    private Double responseCompletenessScore = 0.0;
    
    // Additional metadata
    @ElementCollection
    @CollectionTable(name = "metrics_metadata", joinColumns = @JoinColumn(name = "metrics_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public ReliabilityMetrics() {
        this.createdAt = LocalDateTime.now();
        this.calculatedAt = LocalDateTime.now();
    }
    
    public ReliabilityMetrics(String benchmarkRunId, AgentFrameworkType frameworkType) {
        this();
        this.benchmarkRunId = benchmarkRunId;
        this.frameworkType = frameworkType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBenchmarkRunId() {
        return benchmarkRunId;
    }
    
    public void setBenchmarkRunId(String benchmarkRunId) {
        this.benchmarkRunId = benchmarkRunId;
    }
    
    public AgentFrameworkType getFrameworkType() {
        return frameworkType;
    }
    
    public void setFrameworkType(AgentFrameworkType frameworkType) {
        this.frameworkType = frameworkType;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    public Double getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }
    
    public void setAverageResponseTimeMs(Double averageResponseTimeMs) {
        this.averageResponseTimeMs = averageResponseTimeMs;
    }
    
    public Double getMedianResponseTimeMs() {
        return medianResponseTimeMs;
    }
    
    public void setMedianResponseTimeMs(Double medianResponseTimeMs) {
        this.medianResponseTimeMs = medianResponseTimeMs;
    }
    
    public Long getMinResponseTimeMs() {
        return minResponseTimeMs;
    }
    
    public void setMinResponseTimeMs(Long minResponseTimeMs) {
        this.minResponseTimeMs = minResponseTimeMs;
    }
    
    public Long getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }
    
    public void setMaxResponseTimeMs(Long maxResponseTimeMs) {
        this.maxResponseTimeMs = maxResponseTimeMs;
    }
    
    public Double getConsistencyScore() {
        return consistencyScore;
    }
    
    public void setConsistencyScore(Double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }
    
    public Double getRobustnessIndex() {
        return robustnessIndex;
    }
    
    public void setRobustnessIndex(Double robustnessIndex) {
        this.robustnessIndex = robustnessIndex;
    }
    
    public Double getErrorRate() {
        return errorRate;
    }
    
    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }
    
    public Double getTimeoutRate() {
        return timeoutRate;
    }
    
    public void setTimeoutRate(Double timeoutRate) {
        this.timeoutRate = timeoutRate;
    }
    
    public Double getRetryRate() {
        return retryRate;
    }
    
    public void setRetryRate(Double retryRate) {
        this.retryRate = retryRate;
    }
    
    public Double getAverageMemoryUsageMb() {
        return averageMemoryUsageMb;
    }
    
    public void setAverageMemoryUsageMb(Double averageMemoryUsageMb) {
        this.averageMemoryUsageMb = averageMemoryUsageMb;
    }
    
    public Double getPeakMemoryUsageMb() {
        return peakMemoryUsageMb;
    }
    
    public void setPeakMemoryUsageMb(Double peakMemoryUsageMb) {
        this.peakMemoryUsageMb = peakMemoryUsageMb;
    }
    
    public Double getAverageCpuUsagePercent() {
        return averageCpuUsagePercent;
    }
    
    public void setAverageCpuUsagePercent(Double averageCpuUsagePercent) {
        this.averageCpuUsagePercent = averageCpuUsagePercent;
    }
    
    public Double getPeakCpuUsagePercent() {
        return peakCpuUsagePercent;
    }
    
    public void setPeakCpuUsagePercent(Double peakCpuUsagePercent) {
        this.peakCpuUsagePercent = peakCpuUsagePercent;
    }
    
    public Integer getTotalExecutions() {
        return totalExecutions;
    }
    
    public void setTotalExecutions(Integer totalExecutions) {
        this.totalExecutions = totalExecutions;
    }
    
    public Integer getSuccessfulExecutions() {
        return successfulExecutions;
    }
    
    public void setSuccessfulExecutions(Integer successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }
    
    public Integer getFailedExecutions() {
        return failedExecutions;
    }
    
    public void setFailedExecutions(Integer failedExecutions) {
        this.failedExecutions = failedExecutions;
    }
    
    public Integer getTimeoutExecutions() {
        return timeoutExecutions;
    }
    
    public void setTimeoutExecutions(Integer timeoutExecutions) {
        this.timeoutExecutions = timeoutExecutions;
    }
    
    public Double getOutputQualityScore() {
        return outputQualityScore;
    }
    
    public void setOutputQualityScore(Double outputQualityScore) {
        this.outputQualityScore = outputQualityScore;
    }
    
    public Double getResponseRelevanceScore() {
        return responseRelevanceScore;
    }
    
    public void setResponseRelevanceScore(Double responseRelevanceScore) {
        this.responseRelevanceScore = responseRelevanceScore;
    }
    
    public Double getResponseCompletenessScore() {
        return responseCompletenessScore;
    }
    
    public void setResponseCompletenessScore(Double responseCompletenessScore) {
        this.responseCompletenessScore = responseCompletenessScore;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
    
    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public void calculateBasicMetrics() {
        if (totalExecutions > 0) {
            this.successRate = (double) successfulExecutions / totalExecutions * 100.0;
            this.errorRate = (double) failedExecutions / totalExecutions * 100.0;
            this.timeoutRate = (double) timeoutExecutions / totalExecutions * 100.0;
        }
        this.calculatedAt = LocalDateTime.now();
    }
    
    public double getOverallReliabilityScore() {
        // Weighted composite score considering multiple factors
        double performanceWeight = 0.3;
        double reliabilityWeight = 0.4;
        double qualityWeight = 0.3;
        
        double performanceScore = Math.max(0, 100 - (averageResponseTimeMs / 1000)); // Inverse of response time
        double reliabilityScore = successRate;
        double qualityScore = (outputQualityScore + responseRelevanceScore + responseCompletenessScore) / 3;
        
        return (performanceScore * performanceWeight + 
                reliabilityScore * reliabilityWeight + 
                qualityScore * qualityWeight);
    }
    
    @Override
    public String toString() {
        return "ReliabilityMetrics{" +
                "id=" + id +
                ", frameworkType=" + frameworkType +
                ", successRate=" + successRate + "%" +
                ", averageResponseTime=" + averageResponseTimeMs + "ms" +
                ", totalExecutions=" + totalExecutions +
                '}';
    }
}