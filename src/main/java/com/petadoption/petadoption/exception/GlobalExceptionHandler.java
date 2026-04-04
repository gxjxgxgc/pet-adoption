package com.petadoption.petadoption.exception;

import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());

        // 检查异常是否包含业务状态码（1000+）
        if (e.getCode() != null && e.getCode() >= 1000) {
            // 业务异常，使用异常中的状态码和消息
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getCode(), e.getMessage()));
        } else {
            // 普通异常，使用 BAD_REQUEST 状态码
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源未找到：{}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ResponseCode.NOT_FOUND));
    }

    /**
     * 处理参数验证异常（@Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数验证失败";
        log.error("参数验证异常：{}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.error("参数绑定异常：{}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    /**
     * 处理认证异常（用户名或密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("认证异常：{}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ResponseCode.LOGIN_ERROR));
    }

    /**
     * 处理访问 denied 异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问被拒绝：{}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ResponseCode.FORBIDDEN));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.error("权限不足：{}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ResponseCode.FORBIDDEN));
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        log.error("文件上传异常：{}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error(ResponseCode.BAD_REQUEST, "文件大小超过限制，请上传小于10MB的文件")
        );
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        log.error("数据库异常：{}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error(ResponseCode.BAD_REQUEST, "数据完整性错误，请检查输入数据")
        );
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("系统异常：", e);
        return ResponseEntity.internalServerError().body(
                ApiResponse.error(ResponseCode.ERROR, "服务器错误：" + e.getMessage())
        );
    }
}
