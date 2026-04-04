package com.petadoption.petadoption.response;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserType {

    PERSONAL("PERSONAL", "个人用户"),
    SHELTER("SHELTER", "救助机构");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;
}