package com.agentframework.multi_agent_reliability.adapter;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class OpenAIDirectAdapter implements AgentAdapter {
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    public OpenAIDirectAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String executeTask(String taskInput, String taskDescription, Map<String, String> metadata) throws Exception {
        if (!validateInput(taskInput, taskDescription)) {
            throw new IllegalArgumentException("Invalid task input or description");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-api-key-here")) {
            throw new IllegalArgumentException("OpenAI API key is not configured");
        }
        
        try {
            String prompt = buildPrompt(taskInput, taskDescription, metadata);
            String response = callOpenAIAPI(prompt, metadata);
            return postProcessResult(response, metadata);
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
        return AgentFrameworkType.OPENAI_DIRECT;
    }
    
    @Override
    public boolean isAvailable() {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-api-key-here")) {
            return false;
        }
        
        try {
            // Test with a simple call
            callOpenAIAPI("Hello", new HashMap<>());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("framework", "OpenAI Direct API");
        config.put("version", "v1");
        config.put("type", getFrameworkType().name());
        config.put("description", getFrameworkType().getDescription());
        config.put("available", isAvailable());
        config.put("defaultTimeout", getDefaultTimeoutMs());
        config.put("maxRetries", getMaxRetries());
        config.put("apiEndpoint", OPENAI_API_URL);
        config.put("defaultModel", "gpt-3.5-turbo");
        return config;
    }
    
    @Override
    public long getDefaultTimeoutMs() {
        return 180000L; // 3 minutes for direct API calls
    }
    
    @Override
    public Map<String, String> prepareMetadata(Map<String, String> originalMetadata) {
        Map<String, String> metadata = new HashMap<>();
        if (originalMetadata != null) {
            metadata.putAll(originalMetadata);
        }
        
        // Add OpenAI specific metadata
        metadata.put("adapter", "OpenAIDirect");
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        metadata.put("apiVersion", "v1");
        
        // Set default model if not specified
        if (!metadata.containsKey("model")) {
            metadata.put("model", "gpt-3.5-turbo");
        }
        
        // Set default temperature if not specified
        if (!metadata.containsKey("temperature")) {
            metadata.put("temperature", "0.7");
        }
        
        return metadata;
    }
    
    @Override
    public String postProcessResult(String rawResult, Map<String, String> metadata) {
        if (rawResult == null) {
            return "";
        }
        
        String cleaned = rawResult.trim();
        
        // Add metadata to track processing
        if (metadata != null) {
            metadata.put("resultLength", String.valueOf(cleaned.length()));
            metadata.put("processed", "true");
            metadata.put("apiResponse", "parsed");
        }
        
        return cleaned;
    }
    
    @Override
    public String handleExecutionError(Exception exception, String taskInput, Map<String, String> metadata) {
        String baseError = AgentAdapter.super.handleExecutionError(exception, taskInput, metadata);
        String errorMessage = exception.getMessage().toLowerCase();
        
        if (errorMessage.contains("401") || errorMessage.contains("unauthorized")) {
            return baseError + " - Invalid API key, please check your OpenAI API configuration";
        } else if (errorMessage.contains("429") || errorMessage.contains("rate limit")) {
            return baseError + " - Rate limit exceeded, please wait before retrying";
        } else if (errorMessage.contains("400") || errorMessage.contains("bad request")) {
            return baseError + " - Invalid request format, please check input parameters";
        } else if (errorMessage.contains("quota") || errorMessage.contains("billing")) {
            return baseError + " - Quota exceeded, please check your OpenAI billing status";
        } else if (errorMessage.contains("timeout")) {
            return baseError + " - Request timed out, please try again with simpler input";
        }
        
        return baseError;
    }
    
    private String callOpenAIAPI(String prompt, Map<String, String> metadata) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", metadata.getOrDefault("model", "gpt-3.5-turbo"));
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Map[]{message});
        
        // Add optional parameters
        try {
            if (metadata.containsKey("temperature")) {
                requestBody.put("temperature", Double.parseDouble(metadata.get("temperature")));
            }
            if (metadata.containsKey("maxTokens")) {
                requestBody.put("max_tokens", Integer.parseInt(metadata.get("maxTokens")));
            }
        } catch (NumberFormatException e) {
            // Ignore invalid numeric parameters
        }
        
        String requestJson = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        
        // Make API call
        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL, request, String.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("OpenAI API call failed with status: " + response.getStatusCode());
        }
        
        // Parse response
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode choices = responseJson.get("choices");
        
        if (choices == null || choices.size() == 0) {
            throw new Exception("No response choices received from OpenAI API");
        }
        
        JsonNode firstChoice = choices.get(0);
        JsonNode message_node = firstChoice.get("message");
        
        if (message_node == null) {
            throw new Exception("No message content in OpenAI API response");
        }
        
        String content = message_node.get("content").asText();
        
        // Add usage information to metadata if available
        JsonNode usage = responseJson.get("usage");
        if (usage != null && metadata != null) {
            metadata.put("promptTokens", String.valueOf(usage.get("prompt_tokens").asInt()));
            metadata.put("completionTokens", String.valueOf(usage.get("completion_tokens").asInt()));
            metadata.put("totalTokens", String.valueOf(usage.get("total_tokens").asInt()));
        }
        
        return content;
    }
    
    private String buildPrompt(String taskInput, String taskDescription, Map<String, String> metadata) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // Add system instructions if provided
        if (metadata != null && metadata.containsKey("systemPrompt")) {
            promptBuilder.append("System Instructions: ").append(metadata.get("systemPrompt")).append("\n\n");
        }
        
        // Add task description
        promptBuilder.append("Task: ").append(taskDescription).append("\n\n");
        
        // Add specific instructions
        if (metadata != null && metadata.containsKey("instructions")) {
            promptBuilder.append("Instructions: ").append(metadata.get("instructions")).append("\n\n");
        }
        
        // Add the main input
        promptBuilder.append("Input: ").append(taskInput).append("\n\n");
        
        // Add output format guidance
        if (metadata != null && metadata.containsKey("outputFormat")) {
            promptBuilder.append("Please format your response as: ").append(metadata.get("outputFormat"));
        } else {
            promptBuilder.append("Please provide a clear and helpful response.");
        }
        
        return promptBuilder.toString();
    }
}