package com.agentframework.multi_agent_reliability.controller;

import com.agentframework.multi_agent_reliability.model.AgentFrameworkType;
import com.agentframework.multi_agent_reliability.model.TaskComplexity;
import com.agentframework.multi_agent_reliability.service.AgentOrchestrationService;
import com.agentframework.multi_agent_reliability.service.BenchmarkService;
import com.agentframework.multi_agent_reliability.service.MetricsCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ThymeleafViewController {
    
    private final AgentOrchestrationService orchestrationService;
    private final BenchmarkService benchmarkService;
    private final MetricsCollectionService metricsService;
    
    @Autowired
    public ThymeleafViewController(AgentOrchestrationService orchestrationService,
                                 BenchmarkService benchmarkService,
                                 MetricsCollectionService metricsService) {
        this.orchestrationService = orchestrationService;
        this.benchmarkService = benchmarkService;
        this.metricsService = metricsService;
    }
    
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        // Add data for dashboard
        model.addAttribute("frameworks", AgentFrameworkType.values());
        model.addAttribute("complexities", TaskComplexity.values());
        model.addAttribute("recentExecutions", orchestrationService.getRecentExecutions());
        model.addAttribute("executionStats", orchestrationService.getExecutionStatistics());
        model.addAttribute("activeBenchmarks", benchmarkService.getActiveBenchmarkRuns());
        model.addAttribute("activeTasks", benchmarkService.getActiveBenchmarkTasks());
        model.addAttribute("systemSummary", metricsService.getSystemPerformanceSummary());
        model.addAttribute("frameworkComparison", metricsService.getFrameworkComparison());
        return "dashboard";
    }
    
    @GetMapping("/executions")
    public String executions(Model model) {
        model.addAttribute("frameworks", AgentFrameworkType.values());
        model.addAttribute("executions", orchestrationService.getRecentExecutions());
        model.addAttribute("stats", orchestrationService.getExecutionStatistics());
        return "executions";
    }
    
    @GetMapping("/benchmarks")
    public String benchmarks(Model model) {
        model.addAttribute("frameworks", AgentFrameworkType.values());
        model.addAttribute("complexities", TaskComplexity.values());
        model.addAttribute("tasks", benchmarkService.getAllBenchmarkTasks());
        model.addAttribute("activeBenchmarks", benchmarkService.getActiveBenchmarkRuns());
        return "benchmarks";
    }
    
    @GetMapping("/metrics")
    public String metrics(Model model) {
        model.addAttribute("systemSummary", metricsService.getSystemPerformanceSummary());
        model.addAttribute("comparison", metricsService.getFrameworkComparison());
        model.addAttribute("topPerformers", metricsService.getTopPerformingFrameworks());
        model.addAttribute("distribution", metricsService.getReliabilityDistribution());
        return "metrics";
    }
    
    @GetMapping("/execution/{id}")
    public String executionDetails(@PathVariable Long id, Model model) {
        model.addAttribute("execution", orchestrationService.getExecutionStatus(id));
        return "execution-details";
    }
}
