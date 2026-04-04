package com.petadoption.petadoption.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private Integer code;
    private String message;

    public BaseException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}