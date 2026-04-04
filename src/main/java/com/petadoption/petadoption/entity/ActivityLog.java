package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_log")
@Schema(description = "用户活动日志")
public class ActivityLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("activity_type")
    @Schema(description = "活动类型: favorite-收藏, unfavorite-取消收藏, adoption-领养申请, comment-评论, profile-个人信息更新")
    private String activityType;

    @TableField("target_type")
    @Schema(description = "目标类型: pet-宠物, adoption-领养, comment-评论")
    private String targetType;

    @TableField("target_id")
    @Schema(description = "目标ID")
    private Long targetId;

    @TableField("description")
    @Schema(description = "活动描述")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
