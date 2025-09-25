package com.agentframework.multi_agent_reliability.service;

import com.agentframework.multi_agent_reliability.dto.MetricsResponse;
import com.agentframework.multi_agent_reliability.model.AgentExecution;
import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.model.ExecutionStatus;
import com.agentframework.multi_agent_reliability.model.ReliabilityMetrics;
import com.agentframework.multi_agent_reliability.repository.AgentExecutionRepository;
import com.agentframework.multi_agent_reliability. repository.ReliabilityMetricsRepository;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; 
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MetricsCollectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollectionService.class);
    
    private final AgentExecutionRepository executionRepository;
    private final ReliabilityMetricsRepository metricsRepository;
    private final WebSocketNotificationService notificationService;
    
    @Autowired
    public MetricsCollectionService(
            AgentExecutionRepository executionRepository,
            ReliabilityMetricsRepository metricsRepository,
            WebSocketNotificationService notificationService) {
        this.executionRepository = executionRepository;
        this.metricsRepository = metricsRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Calculate comprehensive metrics for a specific framework in a benchmark run
     */
    @Async("metricsCalculationExecutor")
    public CompletableFuture<ReliabilityMetrics> calculateFrameworkMetrics(String benchmarkRunId, AgentFrameworkType frameworkType) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Calculating metrics for framework {} in benchmark run {}", frameworkType, benchmarkRunId);
            
            try {
                // Get all executions for this framework in the benchmark run
                List<AgentExecution> executions = executionRepository.findExecutionsForReliabilityAnalysis(benchmarkRunId, frameworkType);
                
                if (executions.isEmpty()) {
                    logger.warn("No executions found for framework {} in benchmark run {}", frameworkType, benchmarkRunId);
                    return null;
                }
                
                // Create or update metrics record
                ReliabilityMetrics metrics = metricsRepository
                        .findByBenchmarkRunIdAndFrameworkType(benchmarkRunId, frameworkType)
                        .orElse(new ReliabilityMetrics(benchmarkRunId, frameworkType));
                
                // Calculate all metrics
                calculateBasicMetrics(metrics, executions);
                calculatePerformanceMetrics(metrics, executions);
                calculateAdvancedMetrics(metrics, executions);
                calculateQualityMetrics(metrics, executions);
                calculateResourceMetrics(metrics, executions);
                
                // Save metrics
                metrics = metricsRepository.save(metrics);
                
                // Send real-time update
                notificationService.sendMetricsUpdate(metrics);
                
                logger.info("Metrics calculated successfully for framework {} in benchmark run {}", frameworkType, benchmarkRunId);
                return metrics;
                
            } catch (Exception e) {
                logger.error("Failed to calculate metrics for framework {} in benchmark run {}", frameworkType, benchmarkRunId, e);
                throw new RuntimeException("Metrics calculation failed", e);
            }
        });
    }
    
    /**
     * Calculate metrics for all frameworks in a benchmark run
     */
    @Async("metricsCalculationExecutor")
    public CompletableFuture<List<ReliabilityMetrics>> calculateAllFrameworkMetrics(String benchmarkRunId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Calculating metrics for all frameworks in benchmark run {}", benchmarkRunId);
            
            List<ReliabilityMetrics> allMetrics = new ArrayList<>();
            
            // Get unique frameworks from executions
            List<AgentExecution> allExecutions = executionRepository.findByBenchmarkRunId(benchmarkRunId);
            Set<AgentFrameworkType> frameworks = allExecutions.stream()
                    .map(AgentExecution::getFrameworkType)
                    .collect(Collectors.toSet());
            
            // Calculate metrics for each framework
            for (AgentFrameworkType framework : frameworks) {
                try {
                    ReliabilityMetrics metrics = calculateFrameworkMetrics(benchmarkRunId, framework).get();
                    if (metrics != null) {
                        allMetrics.add(metrics);
                    }
                } catch (Exception e) {
                    logger.error("Failed to calculate metrics for framework {}", framework, e);
                }
            }
            
            logger.info("Calculated metrics for {} frameworks in benchmark run {}", allMetrics.size(), benchmarkRunId);
            return allMetrics;
        });
    }
    
    /**
     * Get metrics comparison across all frameworks
     */
    public List<MetricsResponse> getFrameworkComparison() {
        List<Object[]> comparisonData = metricsRepository.getFrameworkComparisonMetrics();
        List<MetricsResponse> comparison = new ArrayList<>();
        
        for (Object[] data : comparisonData) {
            MetricsResponse response = new MetricsResponse();
            response.setFrameworkType((AgentFrameworkType) data[0]);
            response.setSuccessRate((Double) data[1]);
            response.setAverageResponseTimeMs((Double) data[2]);
            response.setConsistencyScore((Double) data[3]);
            response.setRobustnessIndex((Double) data[4]);
            response.setCalculatedAt(LocalDateTime.now());
            
            comparison.add(response);
        }
        
        return comparison;
    }
    
    /**
     * Get system performance summary
     */
    public Map<String, Object> getSystemPerformanceSummary() {
        List<Object[]> summaryData = metricsRepository.getSystemPerformanceSummary();
        Map<String, Object> summary = new HashMap<>();
        
        if (!summaryData.isEmpty()) {
            Object[] data = summaryData.get(0);
            summary.put("frameworkCount", data[0]);
            summary.put("overallSuccessRate", data[1]);
            summary.put("overallAverageResponseTime", data[2]);
            summary.put("overallConsistencyScore", data[3]);
            summary.put("lastUpdated", LocalDateTime.now());
        }
        
        return summary;
    }
    
    /**
     * Get top performing frameworks
     */
    public List<Map<String, Object>> getTopPerformingFrameworks() {
        List<Object[]> topFrameworks = metricsRepository.getTopPerformingFrameworksByCompositeScore();
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Object[] data : topFrameworks) {
            Map<String, Object> framework = new HashMap<>();
            framework.put("frameworkType", data[0]);
            framework.put("compositeScore", data[1]);
            results.add(framework);
        }
        
        return results;
    }
    
    /**
     * Get reliability distribution
     */
    public Map<String, Object> getReliabilityDistribution() {
        List<Object[]> distribution = metricsRepository.getReliabilityDistribution();
        Map<String, Object> result = new HashMap<>();
        
        for (Object[] data : distribution) {
            result.put((String) data[0], data[1]);
        }
        
        return result;
    }
    
    /**
     * Get performance trends for a specific framework
     */
    public List<Map<String, Object>> getFrameworkPerformanceTrend(AgentFrameworkType frameworkType) {
        List<Object[]> trendData = metricsRepository.getFrameworkPerformanceTrend(frameworkType);
        List<Map<String, Object>> trends = new ArrayList<>();
        
        for (Object[] data : trendData) {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", data[1]);
            point.put("successRate", data[2]);
            point.put("averageResponseTime", data[3]);
            trends.add(point);
        }
        
        return trends;
    }
    
    /**
     * Get statistical summary for a framework
     */
    public Map<String, Object> getFrameworkStatisticalSummary(AgentFrameworkType frameworkType) {
        List<Object[]> summaryData = metricsRepository.getFrameworkStatisticalSummary(frameworkType);
        Map<String, Object> summary = new HashMap<>();
        
        if (!summaryData.isEmpty()) {
            Object[] data = summaryData.get(0);
            summary.put("totalRecords", data[0]);
            summary.put("averageSuccessRate", data[1]);
            summary.put("minSuccessRate", data[2]);
            summary.put("maxSuccessRate", data[3]);
            summary.put("averageResponseTime", data[4]);
            summary.put("minResponseTime", data[5]);
            summary.put("maxResponseTime", data[6]);
        }
        
        return summary;
    }
    
    /**
     * Calculate basic metrics (success rate, counts, etc.)
     */
    private void calculateBasicMetrics(ReliabilityMetrics metrics, List<AgentExecution> executions) {
        int total = executions.size();
        int successful = 0;
        int failed = 0;
        int timeouts = 0;
        
        for (AgentExecution execution : executions) {
            switch (execution.getStatus()) {
                case COMPLETED -> successful++;
                case FAILED -> failed++;
                case TIMEOUT -> timeouts++;
            }
        }
        
        metrics.setTotalExecutions(total);
        metrics.setSuccessfulExecutions(successful);
        metrics.setFailedExecutions(failed);
        metrics.setTimeoutExecutions(timeouts);
        
        if (total > 0) {
            metrics.setSuccessRate((double) successful / total * 100.0);
            metrics.setErrorRate((double) failed / total * 100.0);
            metrics.setTimeoutRate((double) timeouts / total * 100.0);
        }
        
        metrics.calculateBasicMetrics();
    }
    
    /**
     * Calculate performance metrics (response times, etc.)
     */
    private void calculatePerformanceMetrics(ReliabilityMetrics metrics, List<AgentExecution> executions) {
        List<Long> responseTimes = executions.stream()
                .filter(e -> e.getExecutionDurationMs() != null && e.getStatus() == ExecutionStatus.COMPLETED)
                .map(AgentExecution::getExecutionDurationMs)
                .collect(Collectors.toList());
        
        if (!responseTimes.isEmpty()) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            responseTimes.forEach(stats::addValue);
            
            metrics.setAverageResponseTimeMs(stats.getMean());
            metrics.setMedianResponseTimeMs(stats.getPercentile(50));
            metrics.setMinResponseTimeMs((long) stats.getMin());
            metrics.setMaxResponseTimeMs((long) stats.getMax());
        }
    }
    
    /**
     * Calculate advanced metrics (consistency, robustness, etc.)
     */
    private void calculateAdvancedMetrics(ReliabilityMetrics metrics, List<AgentExecution> executions) {
        // Calculate consistency score based on response time variance
        List<Long> responseTimes = executions.stream()
                .filter(e -> e.getExecutionDurationMs() != null && e.getStatus() == ExecutionStatus.COMPLETED)
                .map(AgentExecution::getExecutionDurationMs)
                .collect(Collectors.toList());
        
        if (responseTimes.size() > 1) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            responseTimes.forEach(stats::addValue);
            
            double variance = stats.getVariance();
            double mean = stats.getMean();
            double coefficientOfVariation = variance > 0 ? Math.sqrt(variance) / mean : 0;
            
            // Consistency score: higher is better (lower variability)
            double consistencyScore = Math.max(0, 100 - (coefficientOfVariation * 100));
            metrics.setConsistencyScore(consistencyScore);
        } else {
            metrics.setConsistencyScore(100.0); // Perfect consistency with single execution
        }
        
        // Calculate robustness index based on error handling and recovery
        long retriedExecutions = executions.stream()
                .mapToLong(e -> e.getMetadata() != null && e.getMetadata().containsKey("retryCount") ? 1 : 0)
                .sum();
        
        double retryRate = executions.size() > 0 ? (double) retriedExecutions / executions.size() * 100 : 0;
        metrics.setRetryRate(retryRate);
        
        // Robustness: combination of success rate and low retry rate
        double robustnessIndex = (metrics.getSuccessRate() * 0.7) + ((100 - retryRate) * 0.3);
        metrics.setRobustnessIndex(robustnessIndex);
    }
    
    /**
     * Calculate quality metrics (output quality, relevance, etc.)
     */
    private void calculateQualityMetrics(ReliabilityMetrics metrics, List<AgentExecution> executions) {
        // For now, set baseline quality scores
        // In a real implementation, these would be calculated based on:
        // - Output comparison with expected results
        // - Semantic similarity analysis  
        // - Domain-specific quality assessments
        
        List<AgentExecution> successfulExecutions = executions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.COMPLETED && e.getTaskOutput() != null)
                .collect(Collectors.toList());
        
        if (!successfulExecutions.isEmpty()) {
            // Simple quality metrics based on output length and completeness
            double avgOutputLength = successfulExecutions.stream()
                    .mapToDouble(e -> e.getTaskOutput().length())
                    .average()
                    .orElse(0);
            
            // Basic quality scoring (this would be more sophisticated in production)
            double outputQualityScore = Math.min(100, avgOutputLength / 10); // Simple heuristic
            double relevanceScore = metrics.getSuccessRate() * 0.8; // Correlate with success rate
            double completenessScore = successfulExecutions.stream()
                    .mapToDouble(e -> e.getTaskOutput().trim().isEmpty() ? 0 : 85)
                    .average()
                    .orElse(0);
            
            metrics.setOutputQualityScore(outputQualityScore);
            metrics.setResponseRelevanceScore(relevanceScore);
            metrics.setResponseCompletenessScore(completenessScore);
        }
    }
    
    /**
     * Calculate resource usage metrics
     */
    private void calculateResourceMetrics(ReliabilityMetrics metrics, List<AgentExecution> executions) {
        // Simulate resource usage metrics
        // In production, these would come from actual system monitoring
        
        Runtime runtime = Runtime.getRuntime();
        double usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0; // MB
        double maxMemory = runtime.maxMemory() / 1024.0 / 1024.0; // MB
        
        // Estimate resource usage based on execution patterns
        double avgMemoryUsage = Math.min(usedMemory, maxMemory * 0.3); // Conservative estimate
        double peakMemoryUsage = Math.min(usedMemory * 1.5, maxMemory * 0.6);
        
        // CPU usage estimation based on execution count and duration
        double avgCpuUsage = Math.min(50.0, executions.size() * 2.0); // Simple heuristic
        double peakCpuUsage = Math.min(80.0, avgCpuUsage * 1.8);
        
        metrics.setAverageMemoryUsageMb(avgMemoryUsage);
        metrics.setPeakMemoryUsageMb(peakMemoryUsage);
        metrics.setAverageCpuUsagePercent(avgCpuUsage);
        metrics.setPeakCpuUsagePercent(peakCpuUsage);
    }
}