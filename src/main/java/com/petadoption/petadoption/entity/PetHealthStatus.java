package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_health_status")
public class PetHealthStatus {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long healthStatusId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}