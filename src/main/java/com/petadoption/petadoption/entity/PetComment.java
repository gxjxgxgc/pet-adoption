package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_comment")
@Schema(description = "宠物评论")
public class PetComment {

    @TableId(type = IdType.AUTO)
    @Schema(description = "评论 ID")
    private Long id;

    @TableField("pet_id")
    @Schema(description = "宠物 ID")
    private Long petId;

    @TableField("user_id")
    @Schema(description = "评论用户 ID")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @TableField("parent_id")
    @Schema(description = "父评论 ID")
    private Long parentId;

    @TableField("reply_to_user_id")
    @Schema(description = "被回复用户 ID")
    private Long replyToUserId;

    @TableField("like_count")
    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "状态")
    private Integer status;

    @TableField("create_time")
    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除标记")
    private Integer deleted;
}
