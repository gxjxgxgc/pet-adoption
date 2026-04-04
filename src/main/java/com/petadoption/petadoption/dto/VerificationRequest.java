package com.petadoption.petadoption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "实名认证请求")
public class VerificationRequest {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证正面图片URL")
    private String idFront;

    @Schema(description = "身份证反面图片URL")
    private String idBack;
}
