package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.HealthStatus;
import com.petadoption.petadoption.mapper.HealthStatusMapper;
import com.petadoption.petadoption.service.HealthStatusService;
import org.springframework.stereotype.Service;

@Service
public class HealthStatusServiceImpl extends ServiceImpl<HealthStatusMapper, HealthStatus> implements HealthStatusService {
    
    private final HealthStatusMapper healthStatusMapper;
    
    public HealthStatusServiceImpl(HealthStatusMapper healthStatusMapper) {
        this.healthStatusMapper = healthStatusMapper;
    }
    
    @Override
    public HealthStatus getByCode(String code) {
        return healthStatusMapper.selectByCode(code);
    }
    
    @Override
    public void initHealthStatuses() {
        long count = count();
        if (count > 0) {
            return;
        }
        
        save(createHealthStatus("healthy", "健康", "宠物身体健康"));
        save(createHealthStatus("vaccinated", "已疫苗", "宠物已接种疫苗"));
        save(createHealthStatus("sterilized", "已绝育", "宠物已绝育"));
    }
    
    private HealthStatus createHealthStatus(String code, String name, String description) {
        HealthStatus status = new HealthStatus();
        status.setCode(code);
        status.setName(name);
        status.setDescription(description);
        return status;
    }
}