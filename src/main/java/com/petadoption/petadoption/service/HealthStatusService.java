package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.HealthStatus;

public interface HealthStatusService extends IService<HealthStatus> {
    // 根据代码获取健康状况
    HealthStatus getByCode(String code);
    
    // 初始化健康状况数据
    void initHealthStatuses();
}