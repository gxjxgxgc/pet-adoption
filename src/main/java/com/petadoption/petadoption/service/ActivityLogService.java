package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.ActivityLog;

import java.util.List;

public interface ActivityLogService extends IService<ActivityLog> {
    void logActivity(Long userId, String activityType, String targetType, Long targetId, String description);
    List<ActivityLog> getUserActivities(Long userId, String activityType, Integer page, Integer size);
}
