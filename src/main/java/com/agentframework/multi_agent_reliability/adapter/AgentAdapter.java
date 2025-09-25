package com.agentframework.multi_agent_reliability.adapter;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all agent framework adapters.
 * Provides a unified interface for executing tasks across different AI agent frameworks.
 */
public interface AgentAdapter {
    
    /**
     * Execute a task synchronously
     * @param taskInput The input for the task
     * @param taskDescription Description of the task to be performed
     * @param metadata Additional configuration and parameters
     * @return The task execution result
     * @throws Exception if execution fails
     */
    String executeTask(String taskInput, String taskDescription, Map<String, String> metadata) throws Exception;
    
    /**
     * Execute a task asynchronously
     * @param taskInput The input for the task
     * @param taskDescription Description of the task to be performed
     * @param metadata Additional configuration and parameters
     * @return CompletableFuture containing the task execution result
     */
    CompletableFuture<String> executeTaskAsync(String taskInput, String taskDescription, Map<String, String> metadata);
    
    /**
     * Get the framework type this adapter supports
     * @return The AgentFrameworkType
     */
    AgentFrameworkType getFrameworkType();
    
    /**
     * Check if the adapter is available and properly configured
     * @return true if the adapter is ready to use
     */
    boolean isAvailable();
    
    /**
     * Get adapter-specific configuration information
     * @return Map of configuration details
     */
    Map<String, Object> getConfiguration();
    
    /**
     * Validate task input before execution
     * @param taskInput The input to validate
     * @param taskDescription The task description
     * @return true if input is valid
     */
    default boolean validateInput(String taskInput, String taskDescription) {
        return taskInput != null && !taskInput.trim().isEmpty() &&
               taskDescription != null && !taskDescription.trim().isEmpty();
    }
    
    /**
     * Get default timeout in milliseconds for this adapter
     * @return timeout in milliseconds
     */
    default long getDefaultTimeoutMs() {
        return 300000L; // 5 minutes default
    }
    
    /**
     * Get maximum number of retries for this adapter
     * @return maximum retry count
     */
    default int getMaxRetries() {
        return 3;
    }
    
    /**
     * Prepare metadata for execution, allowing adapter-specific preprocessing
     * @param originalMetadata The original metadata
     * @return Processed metadata
     */
    default Map<String, String> prepareMetadata(Map<String, String> originalMetadata) {
        return originalMetadata;
    }
    
    /**
     * Post-process the execution result, allowing adapter-specific cleanup or formatting
     * @param rawResult The raw execution result
     * @param metadata The execution metadata
     * @return Processed result
     */
    default String postProcessResult(String rawResult, Map<String, String> metadata) {
        return rawResult;
    }
    
    /**
     * Handle execution errors and provide meaningful error messages
     * @param exception The original exception
     * @param taskInput The input that caused the error
     * @param metadata The execution metadata
     * @return Formatted error message
     */
    default String handleExecutionError(Exception exception, String taskInput, Map<String, String> metadata) {
        return String.format("Execution failed for %s: %s", 
                           getFrameworkType().getDisplayName(), 
                           exception.getMessage());
    }
}