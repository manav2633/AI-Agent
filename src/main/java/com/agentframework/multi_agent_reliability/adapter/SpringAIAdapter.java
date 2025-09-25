package com.agentframework.multi_agent_reliability.adapter;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class SpringAIAdapter implements AgentAdapter {
    
    private final ChatClient chatClient;
    
    @Autowired
    public SpringAIAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @Override
    public String executeTask(String taskInput, String taskDescription, Map<String, String> metadata) throws Exception {
        if (!validateInput(taskInput, taskDescription)) {
            throw new IllegalArgumentException("Invalid task input or description");
        }
        
        try {
            String prompt = buildPrompt(taskInput, taskDescription, metadata);
            String result = chatClient.call(prompt);
            return postProcessResult(result, metadata);
        } catch (Exception e) {
            throw new Exception(handleExecutionError(e, taskInput, metadata), e);
        }
    }
    
    @Override
    public CompletableFuture<String> executeTaskAsync(String taskInput, String taskDescription, Map<String, String> metadata) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTask(taskInput, taskDescription, metadata);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public AgentFrameworkType getFrameworkType() {
        return AgentFrameworkType.SPRING_AI;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Test with a simple prompt to check availability
            chatClient.call("Hello");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("framework", "Spring AI");
        config.put("version", "0.8.1");
        config.put("type", getFrameworkType().name());
        config.put("description", getFrameworkType().getDescription());
        config.put("available", isAvailable());
        config.put("defaultTimeout", getDefaultTimeoutMs());
        config.put("maxRetries", getMaxRetries());
        return config;
    }
    
    @Override
    public Map<String, String> prepareMetadata(Map<String, String> originalMetadata) {
        Map<String, String> metadata = new HashMap<>();
        if (originalMetadata != null) {
            metadata.putAll(originalMetadata);
        }
        
        // Add Spring AI specific metadata
        metadata.put("adapter", "SpringAI");
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return metadata;
    }
    
    @Override
    public String postProcessResult(String rawResult, Map<String, String> metadata) {
        if (rawResult == null) {
            return "";
        }
        
        // Clean up the result - remove any unwanted formatting
        String cleaned = rawResult.trim();
        
        // Add metadata to track processing
        if (metadata != null) {
            metadata.put("resultLength", String.valueOf(cleaned.length()));
            metadata.put("processed", "true");
        }
        
        return cleaned;
    }
    
    @Override
    public String handleExecutionError(Exception exception, String taskInput, Map<String, String> metadata) {
        String baseError = AgentAdapter.super.handleExecutionError(exception, taskInput, metadata);
        
        // Add Spring AI specific error context
        if (exception.getMessage().contains("API key")) {
            return baseError + " - Please check your OpenAI API key configuration";
        } else if (exception.getMessage().contains("quota")) {
            return baseError + " - API quota exceeded, please check your usage limits";
        } else if (exception.getMessage().contains("network") || exception.getMessage().contains("timeout")) {
            return baseError + " - Network connectivity issue, please try again";
        }
        
        return baseError;
    }
    
    private String buildPrompt(String taskInput, String taskDescription, Map<String, String> metadata) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // Add task description as context
        promptBuilder.append("Task: ").append(taskDescription).append("\n\n");
        
        // Add any specific instructions from metadata
        if (metadata != null && metadata.containsKey("instructions")) {
            promptBuilder.append("Instructions: ").append(metadata.get("instructions")).append("\n\n");
        }
        
        // Add the main input
        promptBuilder.append("Input: ").append(taskInput).append("\n\n");
        
        // Add output format guidance if specified
        if (metadata != null && metadata.containsKey("outputFormat")) {
            promptBuilder.append("Please format your response as: ").append(metadata.get("outputFormat")).append("\n\n");
        }
        
        return promptBuilder.toString();
    }
}