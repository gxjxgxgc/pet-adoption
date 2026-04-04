package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadoption.petadoption.entity.HealthStatus;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthStatusMapper extends BaseMapper<HealthStatus> {
    // 根据代码获取健康状况
    HealthStatus selectByCode(String code);
}