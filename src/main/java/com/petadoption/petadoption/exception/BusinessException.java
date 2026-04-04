package com.petadoption.petadoption.exception;

import com.petadoption.petadoption.response.ResponseCode;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(400, message);
    }
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getCode(), responseCode.getMessage());
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }
}