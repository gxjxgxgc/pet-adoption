package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_adoption")
public class PetAdoption {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pet_id")
    private Long petId;

    @TableField("user_id")
    private Long userId;

    private Integer status;

    private String reason;

    @TableField("contact_name")
    private String contactName;

    @TableField("phone")
    private String phone;

    @TableField("address")
    private String address;

    @TableField("housing_type")
    private String housingType;

    @TableField("experience")
    private Integer experience;

    @TableField("experience_desc")
    private String experienceDesc;

    @TableField("pet_plan")
    private String petPlan;

    @TableField("work_info")
    private String workInfo;

    @TableField("family_info")
    private String familyInfo;

    @TableField("financial_status")
    private String financialStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("rating")
    private Integer rating;

    @TableField("comment")
    private String comment;

    @TableField("rate_time")
    private LocalDateTime rateTime;
}
