package com.example.batchmonitor.controller;

import com.example.batchmonitor.service.ValidationNotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ValidationNotificationController {

    private final ValidationNotificationService validationNotificationService;

    public ValidationNotificationController(ValidationNotificationService validationNotificationService) {
        this.validationNotificationService = validationNotificationService;
    }

    @GetMapping("/validation-notifications")
    public String notifications(Model model) {
        model.addAttribute("activeMenu", "validationNotifications");
        model.addAttribute("notifications", validationNotificationService.findNotifications());
        return "validation/notifications";
    }

    @PostMapping("/validation-notifications/{notificationId}/read")
    public String markRead(@PathVariable Long notificationId) {
        validationNotificationService.markRead(notificationId);
        return "redirect:/validation-notifications";
    }
}
