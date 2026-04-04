package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.dto.LoginRequest;
import com.petadoption.petadoption.dto.RegisterRequest;
import com.petadoption.petadoption.entity.User;

public interface UserService extends IService<User> {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getUserByUsername(String username);
    void updateLastLoginTime(Long userId);
    void updateCreditScore(Long userId, int delta);
    com.baomidou.mybatisplus.core.metadata.IPage<User> getUserList(int page, int size, String keyword, String userType, Integer status, String startTime, String endTime);
    void updateUserStatus(Long userId, int status);
    void updateUserTags(Long userId, String tags);
    boolean existsByPhone(String phone);
    User submitVerification(Long userId, String realName, String idFront, String idBack);
    User submitManualVerification(Long userId, String realName, String idCard);
    User approveVerification(Long userId, boolean approved);
}
