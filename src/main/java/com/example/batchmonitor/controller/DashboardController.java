package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.SearchConditionDto;
import com.example.batchmonitor.dto.DashboardValidationTrendDto;
import com.example.batchmonitor.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<DashboardValidationTrendDto> validationTrend = dashboardService.getValidationTrend();
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("summary", dashboardService.getSummary());
        model.addAttribute("aiOverview", dashboardService.getAiOverview());
        model.addAttribute("aiMismatchSummary", dashboardService.getAiMismatchSummary());
        model.addAttribute("aiSqlValidation", dashboardService.getAiSqlValidationResult());
        model.addAttribute("validationTrend", validationTrend);
        model.addAttribute("validationTrendTotalCount", dashboardService.getValidationTrendTotalCount(validationTrend));
        model.addAttribute("validationTypeSummary", dashboardService.getValidationTypeSummary());
        model.addAttribute("recentJobs", dashboardService.getRecentJobExecutions());
        model.addAttribute("recentQueryResults", dashboardService.getRecentQueryComparisonResults());
        return "dashboard/index";
    }

    @GetMapping("/dashboard/jobs")
    public String detail(@RequestParam(value = "type", required = false) String type,
                         @ModelAttribute SearchConditionDto condition,
                         Model model) {
        String normalizedType = dashboardService.normalizeDetailType(type);
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("type", normalizedType);
        model.addAttribute("title", dashboardService.getDetailTitle(normalizedType));
        model.addAttribute("condition", condition);
        model.addAttribute("page", dashboardService.getDetailJobs(normalizedType, condition));
        return "dashboard/detail";
    }
}
