// package com.agentframework.multi_agent_reliability.adapter;

// import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
// import dev.langchain4j.model.chat.ChatLanguageModel;
// import dev.langchain4j.data.message.AiMessage;
// import dev.langchain4j.data.message.UserMessage;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.CompletableFuture;

// @Component
// public class LangChain4jAdapter implements AgentAdapter {
    
//     private final ChatLanguageModel chatModel;
    
//     @Autowired
//     public LangChain4jAdapter(ChatLanguageModel chatModel) {
//         this.chatModel = chatModel;
//     }
    
//     @Override
//     public String executeTask(String taskInput, String taskDescription, Map<String, String> metadata) throws Exception {
//         if (!validateInput(taskInput, taskDescription)) {
//             throw new IllegalArgumentException("Invalid task input or description");
//         }
        
//         try {
//             String prompt = buildPrompt(taskInput, taskDescription, metadata);
//             AiMessage response = chatModel.generate(UserMessage.from(prompt));
//             String result = response.text();
//             return postProcessResult(result, metadata);
//         } catch (Exception e) {
//             throw new Exception(handleExecutionError(e, taskInput, metadata), e);
//         }
//     }
    
//     @Override
//     public CompletableFuture<String> executeTaskAsync(String taskInput, String taskDescription, Map<String, String> metadata) {
//         return CompletableFuture.supplyAsync(() -> {
//             try {
//                 return executeTask(taskInput, taskDescription, metadata);
//             } catch (Exception e) {
//                 throw new RuntimeException(e);
//             }
//         });
//     }
    
//     @Override
//     public AgentFrameworkType getFrameworkType() {
//         return AgentFrameworkType.LANGCHAIN4J;
//     }
    
//     @Override
//     public boolean isAvailable() {
//         try {
//             // Test with a simple message to check availability
//             AiMessage response = chatModel.generate(UserMessage.from("Hello"));
//             return response != null && response.text() != null;
//         } catch (Exception e) {
//             return false;
//         }
//     }
    
//     @Override
//     public Map<String, Object> getConfiguration() {
//         Map<String, Object> config = new HashMap<>();
//         config.put("framework", "LangChain4j");
//         config.put("version", "0.25.0");
//         config.put("type", getFrameworkType().name());
//         config.put("description", getFrameworkType().getDescription());
//         config.put("available", isAvailable());
//         config.put("defaultTimeout", getDefaultTimeoutMs());
//         config.put("maxRetries", getMaxRetries());
//         config.put("modelType", "ChatLanguageModel");
//         return config;
//     }
    
//     @Override
//     public long getDefaultTimeoutMs() {
//         return 240000L; // 4 minutes for LangChain4j
//     }
    
//     @Override
//     public Map<String, String> prepareMetadata(Map<String, String> originalMetadata) {
//         Map<String, String> metadata = new HashMap<>();
//         if (originalMetadata != null) {
//             metadata.putAll(originalMetadata);
//         }
        
//         // Add LangChain4j specific metadata
//         metadata.put("adapter", "LangChain4j");
//         metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
//         metadata.put("modelType", "ChatLanguageModel");
        
//         return metadata;
//     }
    
//     @Override
//     public String postProcessResult(String rawResult, Map<String, String> metadata) {
//         if (rawResult == null) {
//             return "";
//         }
        
//         // Clean up the result
//         String cleaned = rawResult.trim();
        
//         // Remove any LangChain4j specific formatting artifacts
//         cleaned = cleaned.replaceAll("^AI: ", "")
//                         .replaceAll("^Assistant: ", "")
//                         .trim();
        
//         // Add metadata to track processing
//         if (metadata != null) {
//             metadata.put("resultLength", String.valueOf(cleaned.length()));
//             metadata.put("processed", "true");
//             metadata.put("cleanupApplied", "true");
//         }
        
//         return cleaned;
//     }
    
//     @Override
//     public String handleExecutionError(Exception exception, String taskInput, Map<String, String> metadata) {
//         String baseError = AgentAdapter.super.handleExecutionError(exception, taskInput, metadata);
        
//         // Add LangChain4j specific error context
//         String errorMessage = exception.getMessage().toLowerCase();
        
//         if (errorMessage.contains("api key") || errorMessage.contains("authentication")) {
//             return baseError + " - Please verify your API key configuration for the underlying model";
//         } else if (errorMessage.contains("rate limit") || errorMessage.contains("quota")) {
//             return baseError + " - Rate limit exceeded, please wait before retrying";
//         } else if (errorMessage.contains("model") && errorMessage.contains("not found")) {
//             return baseError + " - The specified model is not available or not accessible";
//         } else if (errorMessage.contains("timeout")) {
//             return baseError + " - Request timed out, consider increasing timeout or simplifying the task";
//         } else if (errorMessage.contains("token") && errorMessage.contains("limit")) {
//             return baseError + " - Input or output token limit exceeded, please reduce input size";
//         }
        
//         return baseError;
//     }
    
//     private String buildPrompt(String taskInput, String taskDescription, Map<String, String> metadata) {
//         StringBuilder promptBuilder = new StringBuilder();
        
//         // Add system-level instructions if provided
//         if (metadata != null && metadata.containsKey("systemPrompt")) {
//             promptBuilder.append("System: ").append(metadata.get("systemPrompt")).append("\n\n");
//         }
        
//         // Add task description as context
//         promptBuilder.append("Task: ").append(taskDescription).append("\n\n");
        
//         // Add specific instructions from metadata
//         if (metadata != null && metadata.containsKey("instructions")) {
//             promptBuilder.append("Instructions: ").append(metadata.get("instructions")).append("\n\n");
//         }
        
//         // Add examples if provided
//         if (metadata != null && metadata.containsKey("examples")) {
//             promptBuilder.append("Examples: ").append(metadata.get("examples")).append("\n\n");
//         }
        
//         // Add the main input
//         promptBuilder.append("Input: ").append(taskInput).append("\n\n");
        
//         // Add output format guidance if specified
//         if (metadata != null && metadata.containsKey("outputFormat")) {
//             promptBuilder.append("Please format your response as: ").append(metadata.get("outputFormat")).append("\n\n");
//         }
        
//         // Add closing instruction
//         promptBuilder.append("Please provide a clear and concise response:");
        
//         return promptBuilder.toString();
//     }
// }