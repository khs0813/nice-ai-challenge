package com.example.batchmonitor.controller;

import com.example.batchmonitor.service.StepExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StepExecutionController {

    private final StepExecutionService stepExecutionService;

    public StepExecutionController(StepExecutionService stepExecutionService) {
        this.stepExecutionService = stepExecutionService;
    }

    @GetMapping("/steps/{stepExecutionId}")
    public String detail(@PathVariable Long stepExecutionId, Model model) {
        model.addAttribute("activeMenu", "jobs");
        model.addAttribute("step", stepExecutionService.findStepExecutionDetail(stepExecutionId));
        return "step/detail";
    }
}
