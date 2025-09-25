package com.agentframework.multi_agent_reliability.model;

public enum AgentFrameworkType {
    SPRING_AI("Spring AI", "Official Spring framework for AI integration"),
    LANGCHAIN4J("LangChain4j", "Java implementation of LangChain"),
    OPENAI_DIRECT("OpenAI Direct", "Direct OpenAI API integration"),
    CREW_AI("CrewAI", "Multi-agent collaboration framework"),
    AUTOGEN("AutoGen", "Microsoft AutoGen framework"),
    LANGGRAPH("LangGraph", "Graph-based agent orchestration");
    
    private final String displayName;
    private final String description;
    
    AgentFrameworkType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}