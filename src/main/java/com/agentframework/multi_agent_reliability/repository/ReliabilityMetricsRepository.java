package com.agentframework.multi_agent_reliability.repository;

// import com.agentframework.model.ReliabilityMetrics;
// import com.agentframework.model.AgentFrameworkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.model.ReliabilityMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReliabilityMetricsRepository extends JpaRepository<ReliabilityMetrics, Long> {
    
    // Find metrics by benchmark run ID
    List<ReliabilityMetrics> findByBenchmarkRunId(String benchmarkRunId);
    
    // Find metrics by framework type
    List<ReliabilityMetrics> findByFrameworkType(AgentFrameworkType frameworkType);
    
    // Find metrics by benchmark run and framework
    Optional<ReliabilityMetrics> findByBenchmarkRunIdAndFrameworkType(
            String benchmarkRunId, 
            AgentFrameworkType frameworkType
    );
    
    // Find recent metrics
    List<ReliabilityMetrics> findTop10ByOrderByCalculatedAtDesc();
    
    // Find metrics within date range
    List<ReliabilityMetrics> findByCalculatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find metrics with success rate above threshold
    List<ReliabilityMetrics> findBySuccessRateGreaterThanEqual(Double successRateThreshold);
    
    // Find metrics with response time below threshold
    List<ReliabilityMetrics> findByAverageResponseTimeMsLessThanEqual(Double responseTimeThreshold);
    
    // Custom query for framework comparison
    @Query("SELECT m.frameworkType, " +
           "AVG(m.successRate) as avgSuccessRate, " +
           "AVG(m.averageResponseTimeMs) as avgResponseTime, " +
           "AVG(m.consistencyScore) as avgConsistencyScore, " +
           "AVG(m.robustnessIndex) as avgRobustnessIndex " +
           "FROM ReliabilityMetrics m " +
           "GROUP BY m.frameworkType " +
           "ORDER BY avgSuccessRate DESC")
    List<Object[]> getFrameworkComparisonMetrics();
    
    // Get best performing framework by success rate
    @Query("SELECT m.frameworkType, AVG(m.successRate) as avgSuccessRate " +
           "FROM ReliabilityMetrics m " +
           "GROUP BY m.frameworkType " +
           "ORDER BY avgSuccessRate DESC " +
           "LIMIT 1")
    Optional<Object[]> getBestPerformingFrameworkBySuccessRate();
    
    // Get framework performance trends over time
    @Query("SELECT m.frameworkType, m.calculatedAt, m.successRate, m.averageResponseTimeMs " +
           "FROM ReliabilityMetrics m " +
           "WHERE m.frameworkType = :frameworkType " +
           "ORDER BY m.calculatedAt ASC")
    List<Object[]> getFrameworkPerformanceTrend(@Param("frameworkType") AgentFrameworkType frameworkType);
    
    // Get overall system performance summary
    @Query("SELECT " +
           "COUNT(DISTINCT m.frameworkType) as frameworkCount, " +
           "AVG(m.successRate) as overallSuccessRate, " +
           "AVG(m.averageResponseTimeMs) as overallAvgResponseTime, " +
           "AVG(m.consistencyScore) as overallConsistencyScore " +
           "FROM ReliabilityMetrics m")
    List<Object[]> getSystemPerformanceSummary();
    
    // Find metrics for specific benchmark runs
    @Query("SELECT m FROM ReliabilityMetrics m WHERE m.benchmarkRunId IN :benchmarkRunIds")
    List<ReliabilityMetrics> findByBenchmarkRunIds(@Param("benchmarkRunIds") List<String> benchmarkRunIds);
    
    // Get top performing frameworks by composite score
    @Query("SELECT m.frameworkType, " +
           "AVG((m.successRate * 0.4 + (100 - LEAST(m.averageResponseTimeMs/1000, 100)) * 0.3 + " +
           "m.consistencyScore * 0.3)) as compositeScore " +
           "FROM ReliabilityMetrics m " +
           "GROUP BY m.frameworkType " +
           "ORDER BY compositeScore DESC")
    List<Object[]> getTopPerformingFrameworksByCompositeScore();
    
    // Get reliability distribution
    @Query("SELECT " +
           "CASE " +
           "WHEN m.successRate >= 90 THEN 'Excellent' " +
           "WHEN m.successRate >= 75 THEN 'Good' " +
           "WHEN m.successRate >= 50 THEN 'Fair' " +
           "ELSE 'Poor' " +
           "END as reliabilityCategory, " +
           "COUNT(m) as count " +
           "FROM ReliabilityMetrics m " +
           "GROUP BY reliabilityCategory")
    List<Object[]> getReliabilityDistribution();
    
    // Find metrics needing attention (low success rate or high response time)
    @Query("SELECT m FROM ReliabilityMetrics m " +
           "WHERE m.successRate < :minSuccessRate " +
           "OR m.averageResponseTimeMs > :maxResponseTime " +
           "ORDER BY m.successRate ASC, m.averageResponseTimeMs DESC")
    List<ReliabilityMetrics> findMetricsNeedingAttention(
            @Param("minSuccessRate") Double minSuccessRate,
            @Param("maxResponseTime") Double maxResponseTime
    );
    
    // Delete old metrics (for cleanup)
    void deleteByCalculatedAtBefore(LocalDateTime cutoffDate);
    
    // Check if metrics exist for specific benchmark run and framework
    boolean existsByBenchmarkRunIdAndFrameworkType(String benchmarkRunId, AgentFrameworkType frameworkType);
    
    // Get statistical summary for a framework
    @Query("SELECT " +
           "COUNT(m) as totalRecords, " +
           "AVG(m.successRate) as avgSuccessRate, " +
           "MIN(m.successRate) as minSuccessRate, " +
           "MAX(m.successRate) as maxSuccessRate, " +
           "AVG(m.averageResponseTimeMs) as avgResponseTime, " +
           "MIN(m.averageResponseTimeMs) as minResponseTime, " +
           "MAX(m.averageResponseTimeMs) as maxResponseTime " +
           "FROM ReliabilityMetrics m " +
           "WHERE m.frameworkType = :frameworkType")
    List<Object[]> getFrameworkStatisticalSummary(@Param("frameworkType") AgentFrameworkType frameworkType);
}