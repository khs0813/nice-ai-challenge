package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.service.JobExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class JobExecutionController {

    private final JobExecutionService jobExecutionService;

    public JobExecutionController(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    @GetMapping("/jobs")
    public String list(@ModelAttribute SearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "jobs");
        model.addAttribute("condition", condition);
        model.addAttribute("page", jobExecutionService.findJobExecutions(condition));
        return "job/list";
    }

    @GetMapping("/jobs/failed")
    public String failed(@ModelAttribute SearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "failed");
        model.addAttribute("condition", condition);
        model.addAttribute("page", jobExecutionService.findFailedJobExecutions(condition));
        return "job/failed";
    }

    @GetMapping("/jobs/running")
    public String running(@ModelAttribute SearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "running");
        model.addAttribute("condition", condition);
        model.addAttribute("page", jobExecutionService.findRunningJobExecutions(condition));
        return "job/running";
    }

    @GetMapping("/jobs/{jobExecutionId}")
    public String detail(@PathVariable Long jobExecutionId, Model model) {
        model.addAttribute("activeMenu", "jobs");
        model.addAttribute("job", jobExecutionService.findJobExecutionDetail(jobExecutionId));
        return "job/detail";
    }
}
