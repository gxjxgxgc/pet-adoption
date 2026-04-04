package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.ActivityLog;
import com.petadoption.petadoption.mapper.ActivityLogMapper;
import com.petadoption.petadoption.service.ActivityLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogServiceImpl extends ServiceImpl<ActivityLogMapper, ActivityLog> implements ActivityLogService {

    @Override
    public void logActivity(Long userId, String activityType, String targetType, Long targetId, String description) {
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setActivityType(activityType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        save(log);
    }

    @Override
    public List<ActivityLog> getUserActivities(Long userId, String activityType, Integer page, Integer size) {
        LambdaQueryWrapper<ActivityLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityLog::getUserId, userId);
        if (activityType != null && !activityType.isEmpty()) {
            wrapper.eq(ActivityLog::getActivityType, activityType);
        }
        wrapper.orderByDesc(ActivityLog::getCreateTime);
        
        if (page != null && size != null) {
            Page<ActivityLog> pageObj = new Page<>(page, size);
            return page(pageObj, wrapper).getRecords();
        }
        
        return list(wrapper);
    }
}
