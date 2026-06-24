package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.ValidationNotificationDto;
import com.example.batchmonitor.mapper.ValidationNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ValidationNotificationService {

    private final ValidationNotificationMapper validationNotificationMapper;

    public ValidationNotificationService(ValidationNotificationMapper validationNotificationMapper) {
        this.validationNotificationMapper = validationNotificationMapper;
    }

    @Transactional
    public void notifyResult(String validationType, Long ruleId, Long resultId, String resultStatus,
                             String ruleName, String message, String recipients) {
        ValidationNotificationDto notification = new ValidationNotificationDto();
        notification.setValidationType(validationType);
        notification.setRuleId(ruleId);
        notification.setResultId(resultId);
        notification.setResultStatus(resultStatus);
        notification.setTitle("[" + statusLabel(resultStatus) + "] " + ruleName);
        notification.setMessage(message);
        notification.setRecipients(recipients);
        validationNotificationMapper.insertNotification(notification);
    }

    public List<ValidationNotificationDto> findNotifications() {
        return validationNotificationMapper.findNotifications();
    }

    @Transactional
    public void markRead(Long notificationId) {
        validationNotificationMapper.markRead(notificationId);
    }

    private String statusLabel(String resultStatus) {
        if ("SUCCESS".equalsIgnoreCase(resultStatus)) {
            return "성공";
        }
        if ("FAIL".equalsIgnoreCase(resultStatus)) {
            return "불일치";
        }
        if ("ERROR".equalsIgnoreCase(resultStatus)) {
            return "오류";
        }
        return resultStatus;
    }
}
