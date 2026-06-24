package com.example.batchmonitor.controller;

import com.example.batchmonitor.dto.QueryExpectationDto;
import com.example.batchmonitor.dto.QueryExpectationSearchConditionDto;
import com.example.batchmonitor.service.QueryExpectationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QueryExpectationController {

    private final QueryExpectationService queryExpectationService;

    public QueryExpectationController(QueryExpectationService queryExpectationService) {
        this.queryExpectationService = queryExpectationService;
    }

    @GetMapping("/query-expectations")
    public String list(@ModelAttribute QueryExpectationSearchConditionDto condition,
                       @RequestParam(required = false) Long resultId,
                       Model model) {
        model.addAttribute("expectation", new QueryExpectationDto());
        prepareListModel(condition, resultId, model);
        return "query-expectation/list";
    }

    @GetMapping("/query-expectations/{expectationId}/edit")
    public String edit(@PathVariable Long expectationId,
                       @ModelAttribute QueryExpectationSearchConditionDto condition,
                       Model model) {
        model.addAttribute("activeMenu", "queryExpectations");
        model.addAttribute("condition", condition);
        model.addAttribute("expectation", queryExpectationService.findExpectationById(expectationId));
        model.addAttribute("latestResult", null);
        model.addAttribute("page", queryExpectationService.findExpectations(condition));
        return "query-expectation/list";
    }

    @PostMapping("/query-expectations")
    public String save(@ModelAttribute QueryExpectationDto expectation, Authentication authentication, Model model) {
        try {
            queryExpectationService.saveExpectation(expectation, username(authentication));
            return "redirect:/query-expectations";
        } catch (IllegalArgumentException e) {
            model.addAttribute("expectation", expectation);
            model.addAttribute("formError", e.getMessage());
            prepareListModel(new QueryExpectationSearchConditionDto(), null, model);
            return "query-expectation/list";
        }
    }

    @PostMapping("/query-expectations/{expectationId}/delete")
    public String delete(@PathVariable Long expectationId) {
        queryExpectationService.deleteExpectation(expectationId);
        return "redirect:/query-expectations";
    }

    @PostMapping("/query-expectations/{expectationId}/request")
    public String request(@PathVariable Long expectationId, Authentication authentication) {
        Long resultId = queryExpectationService.requestExpectation(expectationId, username(authentication));
        return "redirect:/query-expectations?resultId=" + resultId;
    }

    @GetMapping("/query-expectation-results")
    public String results(@ModelAttribute QueryExpectationSearchConditionDto condition, Model model) {
        model.addAttribute("activeMenu", "validationResults");
        model.addAttribute("condition", condition);
        model.addAttribute("page", queryExpectationService.findResults(condition));
        return "query-expectation/results";
    }

    @GetMapping("/query-expectation-results/{resultId}")
    public String resultDetail(@PathVariable Long resultId, Model model) {
        model.addAttribute("activeMenu", "validationResults");
        model.addAttribute("result", queryExpectationService.findResultDetail(resultId));
        return "query-expectation/result-detail";
    }

    private String username(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }

    private void prepareListModel(QueryExpectationSearchConditionDto condition, Long resultId, Model model) {
        model.addAttribute("activeMenu", "queryExpectations");
        model.addAttribute("condition", condition);
        model.addAttribute("latestResult", queryExpectationService.findResultById(resultId));
        model.addAttribute("page", queryExpectationService.findExpectations(condition));
    }
}
