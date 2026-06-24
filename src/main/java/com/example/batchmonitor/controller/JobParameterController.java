package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.service.JobParameterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class JobParameterController {

    private final JobParameterService jobParameterService;

    public JobParameterController(JobParameterService jobParameterService) {
        this.jobParameterService = jobParameterService;
    }

    @GetMapping("/parameters")
    public String list(@ModelAttribute SearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "parameters");
        model.addAttribute("condition", condition);
        model.addAttribute("page", jobParameterService.findJobParameters(condition));
        return "parameter/list";
    }
}
