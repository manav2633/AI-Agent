package com.agentframework.multi_agent_reliability.repository;

// import com.agentframework.model.BenchmarkTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agentframework.multi_agent_reliability.model.BenchmarkTask;
import com.agentframework.multi_agent_reliability.model.TaskComplexity;

import java.util.List;
import java.util.Optional;

@Repository
public interface BenchmarkTaskRepository extends JpaRepository<BenchmarkTask, Long> {
    
    // Find active tasks
    List<BenchmarkTask> findByActiveTrue();
    
    // Find by complexity level
    // List<BenchmarkTask> findByComplexity(TaskComplexity complexity);
    
    // Find by name (case insensitive)
    Optional<BenchmarkTask> findByNameIgnoreCase(String name);
    
    // Find tasks by name pattern
    List<BenchmarkTask> findByNameContainingIgnoreCase(String namePattern);
    
    // Find tasks by description pattern
    List<BenchmarkTask> findByDescriptionContainingIgnoreCase(String descriptionPattern);
    
    // Find active tasks by complexity
    // List<BenchmarkTask> findByActiveTrueAndComplexity(TaskComplexity complexity);
    
    // Find tasks created by specific user
    List<BenchmarkTask> findByCreatedBy(String createdBy);
    
    // Find tasks with timeout greater than specified value
    List<BenchmarkTask> findByTimeoutMsGreaterThan(Long timeoutMs);
    
    // Find tasks with maximum retries
    List<BenchmarkTask> findByMaxRetriesLessThanEqual(Integer maxRetries);
    
    // Custom query to get task statistics
    @Query("SELECT COUNT(t) as total, " +
           "SUM(CASE WHEN t.active = true THEN 1 ELSE 0 END) as active, " +
           "SUM(CASE WHEN t.active = false THEN 1 ELSE 0 END) as inactive " +
           "FROM BenchmarkTask t")
    List<Object[]> getTaskStatistics();
    
    // Count tasks by complexity
    @Query("SELECT t.complexity, COUNT(t) FROM BenchmarkTask t GROUP BY t.complexity")
    List<Object[]> countTasksByComplexity();
    
    // Find tasks suitable for benchmarking (active with expected output)
    @Query("SELECT t FROM BenchmarkTask t WHERE t.active = true AND t.expectedOutput IS NOT NULL")
    List<BenchmarkTask> findTasksForBenchmarking();
    
    // Check if task name already exists (case insensitive)
    boolean existsByNameIgnoreCase(String name);
    
    // Find tasks ordered by creation date
    List<BenchmarkTask> findAllByOrderByCreatedAtDesc();
    
    // Find recently created tasks
    @Query("SELECT t FROM BenchmarkTask t ORDER BY t.createdAt DESC LIMIT 10")
    List<BenchmarkTask> findRecentTasks();

    List<BenchmarkTask> findByActiveTrueAndComplexity(TaskComplexity complexity);
}