package com.petadoption.petadoption.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // ==================== HTTP 标准状态码 ====================
SUCCESS(200, "操作成功！"),
BAD_REQUEST(400, "请求参数错误！"),
UNAUTHORIZED(401, "未登录或登录已过期！"),
FORBIDDEN(403, "没有权限！"),
NOT_FOUND(404, "资源不存在！"),
ERROR(500, "操作失败！"),

// ==================== 用户模块（1000-1999）====================
USER_NOT_FOUND(1001, "用户不存在！"),
USER_ALREADY_EXISTS(1002, "用户名已存在！"),
USER_PHONE_EXISTS(1006, "手机号已被注册！"),
PASSWORD_ERROR(1003, "密码错误！"),
ACCOUNT_DISABLED(1004, "账号已被停用或锁定！"),
TOKEN_INVALID(1005, "Token 无效！"),
PARAM_ERROR(1006, "参数错误！"),

// ==================== 宠物模块（2000-2999）====================
PET_NOT_FOUND(2001, "宠物不存在！"),
PET_ALREADY_ADOPTED(2002, "宠物已被领养！"),
PET_NOT_AVAILABLE(2003, "宠物不可领养！"),
PET_STATUS_ERROR(2004, "宠物状态异常！"),

// ==================== 领养模块（3000-3999）====================
ADOPTION_NOT_FOUND(3001, "领养申请不存在！"),
ADOPTION_ALREADY_EXISTS(3002, "已提交过领养申请！"),
ADOPTION_STATUS_ERROR(3003, "领养状态异常！"),
ADOPTION_FAILED(3004, "领养申请失败！"),

VERIFICATION_SUBMIT_FAILED(5001, "实名认证提交失败！"),
VERIFICATION_NOT_FOUND(5002, "实名认证信息不存在！"),
VERIFICATION_ALREADY_SUBMITTED(5003, "已提交过实名认证申请！"),
ID_CARD_INVALID(5004, "身份证号码无效！"),
ID_CARD_RECOGNITION_FAILED(5005, "身份证识别失败，请上传清晰的身份证图片！"),

// ==================== 收藏模块（4000-4999）====================
FAVORITE_ALREADY_EXISTS(4001, "已经收藏过该宠物！"),
FAVORITE_NOT_FOUND(4002, "未收藏该宠物！"),

// ==================== 评论模块（5000-5999）====================
COMMENT_NOT_FOUND(5001, "评论不存在！"),
COMMENT_PERMISSION_DENIED(5002, "评论权限不足！"),

// ==================== 文件上传模块（6000-6999）====================
FILE_UPLOAD_ERROR(6001, "文件上传失败！"),
FILE_TYPE_NOT_ALLOWED(6002, "不支持的文件类型！"),
FILE_SIZE_EXCEEDED(6003, "文件大小超出限制！"),
    LOGIN_ERROR(6004, "登录失败！"),
    PERMISSION_DENIED(6005, "权限不足！");


    private final Integer code;
    private final String message; // Changed from description to message

}
