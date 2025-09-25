package com.agentframework.multi_agent_reliability.dto;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public class MetricsResponse {
    
    private Long id;
    private String benchmarkRunId;
    private AgentFrameworkType frameworkType;
    
    // Core Reliability Metrics
    private Double successRate;
    private Double averageResponseTimeMs;
    private Double medianResponseTimeMs;
    private Long minResponseTimeMs;
    private Long maxResponseTimeMs;
    
    // Advanced Reliability Metrics
    private Double consistencyScore;
    private Double robustnessIndex;
    private Double errorRate;
    private Double timeoutRate;
    
    // Statistical Data
    private Integer totalExecutions;
    private Integer successfulExecutions;
    private Integer failedExecutions;
    private Integer timeoutExecutions;
    
    // Quality Metrics
    private Double outputQualityScore;
    private Double responseRelevanceScore;
    private Double responseCompletenessScore;
    
    // Resource Usage
    private Double averageMemoryUsageMb;
    private Double peakMemoryUsageMb;
    private Double averageCpuUsagePercent;
    private Double peakCpuUsagePercent;
    
    // Additional metadata
    private Map<String, String> metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculatedAt;
    
    // Constructors
    public MetricsResponse() {}
    
    public MetricsResponse(String benchmarkRunId, AgentFrameworkType frameworkType) {
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
    
    // Utility methods
    public double getOverallReliabilityScore() {
        if (successRate == null || averageResponseTimeMs == null) return 0.0;
        
        double performanceWeight = 0.3;
        double reliabilityWeight = 0.4;
        double qualityWeight = 0.3;
        
        double performanceScore = Math.max(0, 100 - (averageResponseTimeMs / 1000));
        double reliabilityScore = successRate;
        double qualityScore = 0.0;
        
        if (outputQualityScore != null && responseRelevanceScore != null && responseCompletenessScore != null) {
            qualityScore = (outputQualityScore + responseRelevanceScore + responseCompletenessScore) / 3;
        } else {
            qualityScore = reliabilityScore; // Fallback to reliability score
        }
        
        return (performanceScore * performanceWeight + 
                reliabilityScore * reliabilityWeight + 
                qualityScore * qualityWeight);
    }
    
    public String getPerformanceGrade() {
        double score = getOverallReliabilityScore();
        if (score >= 90) return "A+";
        if (score >= 85) return "A";
        if (score >= 80) return "B+";
        if (score >= 75) return "B";
        if (score >= 70) return "C+";
        if (score >= 65) return "C";
        if (score >= 60) return "D";
        return "F";
    }
    
    public String getFormattedAverageResponseTime() {
        if (averageResponseTimeMs == null) return "N/A";
        
        if (averageResponseTimeMs < 1000) {
            return String.format("%.0f ms", averageResponseTimeMs);
        } else {
            return String.format("%.1f s", averageResponseTimeMs / 1000);
        }
    }
    
    @Override
    public String toString() {
        return "MetricsResponse{" +
                "frameworkType=" + frameworkType +
                ", successRate=" + successRate + "%" +
                ", averageResponseTime=" + getFormattedAverageResponseTime() +
                ", grade=" + getPerformanceGrade() +
                '}';
    }
}