package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.ValidationNotificationDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ValidationNotificationMapper {

    void insertNotification(ValidationNotificationDto notification);

    List<ValidationNotificationDto> findNotifications();

    void markRead(Long notificationId);
}
