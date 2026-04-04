package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_favorite")
@Schema(description = "宠物收藏")
public class PetFavorite {

    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏 ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户 ID")
    private Long userId;

    @TableField("pet_id")
    @Schema(description = "宠物 ID")
    private Long petId;

    @TableField("create_time")
    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
