package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.QueryComparisonDto;
import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.service.QueryComparisonService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QueryComparisonController {

    private final QueryComparisonService queryComparisonService;

    public QueryComparisonController(QueryComparisonService queryComparisonService) {
        this.queryComparisonService = queryComparisonService;
    }

    @GetMapping("/query-comparisons")
    public String list(@ModelAttribute QueryComparisonSearchConditionDto condition,
                       @RequestParam(required = false) Long resultId,
                       Model model) {
        model.addAttribute("comparison", new QueryComparisonDto());
        prepareListModel(condition, resultId, model);
        return "query-comparison/list";
    }

    @GetMapping("/query-comparisons/{comparisonId}/edit")
    public String edit(@PathVariable Long comparisonId,
                       @ModelAttribute QueryComparisonSearchConditionDto condition,
                       Model model) {
        model.addAttribute("activeMenu", "queryComparisons");
        model.addAttribute("condition", condition);
        model.addAttribute("comparison", queryComparisonService.findComparisonById(comparisonId));
        model.addAttribute("latestResult", null);
        model.addAttribute("page", queryComparisonService.findComparisons(condition));
        return "query-comparison/list";
    }

    @PostMapping("/query-comparisons")
    public String save(@ModelAttribute QueryComparisonDto comparison, Authentication authentication, Model model) {
        try {
            queryComparisonService.saveComparison(comparison, username(authentication));
            return "redirect:/query-comparisons";
        } catch (IllegalArgumentException e) {
            model.addAttribute("comparison", comparison);
            model.addAttribute("formError", e.getMessage());
            prepareListModel(new QueryComparisonSearchConditionDto(), null, model);
            return "query-comparison/list";
        }
    }

    @PostMapping("/query-comparisons/{comparisonId}/delete")
    public String delete(@PathVariable Long comparisonId) {
        queryComparisonService.deleteComparison(comparisonId);
        return "redirect:/query-comparisons";
    }

    @PostMapping("/query-comparisons/{comparisonId}/request")
    public String request(@PathVariable Long comparisonId, Authentication authentication) {
        Long resultId = queryComparisonService.requestComparison(comparisonId, username(authentication));
        return "redirect:/query-comparisons?resultId=" + resultId;
    }

    @GetMapping("/query-comparison-results")
    public String results(@ModelAttribute QueryComparisonSearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "validationResults");
        model.addAttribute("condition", condition);
        model.addAttribute("page", queryComparisonService.findResults(condition));
        return "query-comparison/results";
    }

    @GetMapping("/query-comparison-results/{resultId}")
    public String resultDetail(@PathVariable Long resultId, Model model) {
        model.addAttribute("activeMenu", "validationResults");
        model.addAttribute("result", queryComparisonService.findResultDetail(resultId));
        return "query-comparison/result-detail";
    }

    private String username(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }

    private void prepareListModel(QueryComparisonSearchConditionDto condition, Long resultId, Model model) {
        model.addAttribute("activeMenu", "queryComparisons");
        model.addAttribute("condition", condition);
        model.addAttribute("latestResult", queryComparisonService.findResultById(resultId));
        model.addAttribute("page", queryComparisonService.findComparisons(condition));
    }
}
