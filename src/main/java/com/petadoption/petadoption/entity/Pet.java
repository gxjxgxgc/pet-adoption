package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pet_pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String breed;

    private Integer age;

    private Integer gender;

    private String color;

    private BigDecimal weight;

    private String description;

    private String images;

    private Integer status;

    // 健康状况关联（可选，用于方便查询）
    @TableField(exist = false)
    private List<HealthStatus> healthStatuses;

    private Integer sterilized;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("shelter_id")
    private Long shelterId;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("favorite_count")
    private Integer favoriteCount;
}