package com.petadoption.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_user")
@Schema(title = "用户信息")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @TableField(exist = false)
    @Schema(description = "新密码（仅用于修改密码，不存储）")
    private String newPassword;

    @TableField(exist = false)
    @Schema(description = "原密码（仅用于修改密码验证，不存储）")
    private String oldPassword;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "出生日期")
    private LocalDate birthday;

    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;

    @TableField(exist = false)
    @Schema(description = "身份证正面图片URL")
    private String idFront;

    @TableField(exist = false)
    @Schema(description = "身份证反面图片URL")
    private String idBack;

    @TableField("is_verified")
    @Schema(description = "是否实名认证")
    private Integer isVerified;

    @TableField("user_type")
    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "地址")
    private String address;

    @TableField("credit_score")
    @Schema(description = "信用分")
    private Integer creditScore;

    @TableField("favorite_count")
    @Schema(description = "收藏数")
    private Integer favoriteCount;

    @TableField("adoption_count")
    @Schema(description = "领养数")
    private Integer adoptionCount;

    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除标记")
    private Integer deleted;

    @Schema(description = "用户标签")
    private String tags;
}
