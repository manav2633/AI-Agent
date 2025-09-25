package com.agentframework.multi_agent_reliability.repository;

// import com.agentframework.model.AgentExecution;
// import com.agentframework.model.AgentFrameworkType;
// import com.agentframework.model.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agentframework.multi_agent_reliability.model.AgentExecution;
import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;

import ch.qos.logback.classic.spi.Configurator.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentExecutionRepository extends JpaRepository<AgentExecution, Long> {
    
    // Find by framework type
    List<AgentExecution> findByFrameworkType(AgentFrameworkType frameworkType);
    
    // Find by status
    List<AgentExecution> findByStatus(ExecutionStatus status);
    
    // Find by benchmark run ID
    List<AgentExecution> findByBenchmarkRunId(String benchmarkRunId);
    
    // Find by framework type and status
    List<AgentExecution> findByFrameworkTypeAndStatus(AgentFrameworkType frameworkType, ExecutionStatus status);
    
    // Find executions within date range
    List<AgentExecution> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent executions
    List<AgentExecution> findTop10ByOrderByCreatedAtDesc();
    
    // Find executions by framework type within date range
    List<AgentExecution> findByFrameworkTypeAndCreatedAtBetween(
            AgentFrameworkType frameworkType, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    // Custom query for execution statistics
    @Query("SELECT e.frameworkType, COUNT(e) as total, " +
           "SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
           "SUM(CASE WHEN e.status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
           "AVG(e.executionDurationMs) as avgDuration " +
           "FROM AgentExecution e " +
           "WHERE e.benchmarkRunId = :benchmarkRunId " +
           "GROUP BY e.frameworkType")
    List<Object[]> getExecutionStatsByBenchmarkRun(@Param("benchmarkRunId") String benchmarkRunId);
    
    // Find successful executions for performance analysis
    @Query("SELECT e FROM AgentExecution e WHERE e.status = 'COMPLETED' " +
           "AND e.frameworkType = :frameworkType " +
           "AND e.executionDurationMs IS NOT NULL " +
           "ORDER BY e.executionDurationMs")
    List<AgentExecution> findSuccessfulExecutionsByFramework(@Param("frameworkType") AgentFrameworkType frameworkType);
    
    // Count executions by status for a specific framework
    @Query("SELECT e.status, COUNT(e) FROM AgentExecution e " +
           "WHERE e.frameworkType = :frameworkType " +
           "GROUP BY e.status")
    List<Object[]> countExecutionsByStatusAndFramework(@Param("frameworkType") AgentFrameworkType frameworkType);
    
    // Find executions with execution duration
    List<AgentExecution> findByExecutionDurationMsIsNotNull();
    
    // Find executions by task description pattern
    List<AgentExecution> findByTaskDescriptionContainingIgnoreCase(String taskKeyword);
    
    // Find executions that took longer than specified duration
    List<AgentExecution> findByExecutionDurationMsGreaterThan(Long durationMs);
    
    // Find failed executions with error messages
    List<AgentExecution> findByStatusAndErrorMessageIsNotNull(ExecutionStatus status);
    
    // Average execution time by framework type
    @Query("SELECT e.frameworkType, AVG(e.executionDurationMs) " +
           "FROM AgentExecution e " +
           "WHERE e.status = 'COMPLETED' AND e.executionDurationMs IS NOT NULL " +
           "GROUP BY e.frameworkType")
    List<Object[]> getAverageExecutionTimeByFramework();
    
    // Success rate by framework type
    @Query("SELECT e.frameworkType, " +
           "COUNT(e) as total, " +
           "SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) as successful, " +
           "(SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / COUNT(e)) as successRate " +
           "FROM AgentExecution e " +
           "GROUP BY e.frameworkType")
    List<Object[]> getSuccessRateByFramework();
    
    // Find executions for reliability analysis
    @Query("SELECT e FROM AgentExecution e " +
           "WHERE e.benchmarkRunId = :benchmarkRunId " +
           "AND e.frameworkType = :frameworkType " +
           "AND e.status IN ('COMPLETED', 'FAILED') " +
           "ORDER BY e.createdAt")
    List<AgentExecution> findExecutionsForReliabilityAnalysis(
            @Param("benchmarkRunId") String benchmarkRunId,
            @Param("frameworkType") AgentFrameworkType frameworkType
    );
    
    // Delete old executions (for cleanup)
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    // Check if execution exists for specific benchmark and framework
    boolean existsByBenchmarkRunIdAndFrameworkType(String benchmarkRunId, AgentFrameworkType frameworkType);
    
    // Find the latest execution for each framework type
    @Query("SELECT e FROM AgentExecution e " +
           "WHERE e.createdAt = (SELECT MAX(e2.createdAt) FROM AgentExecution e2 WHERE e2.frameworkType = e.frameworkType)")
    List<AgentExecution> findLatestExecutionByFramework();
}