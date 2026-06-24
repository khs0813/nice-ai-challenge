package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.service.ValidationResultService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class ValidationResultController {

    private final ValidationResultService validationResultService;

    public ValidationResultController(ValidationResultService validationResultService) {
        this.validationResultService = validationResultService;
    }

    @GetMapping("/validation-results")
    public String results(@ModelAttribute QueryComparisonSearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "validationResults");
        model.addAttribute("condition", condition);
        model.addAttribute("page", validationResultService.findResults(condition));
        return "validation/results";
    }
}
