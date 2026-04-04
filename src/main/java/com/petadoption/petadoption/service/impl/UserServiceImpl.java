package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.dto.LoginRequest;
import com.petadoption.petadoption.dto.RegisterRequest;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.UserMapper;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.OcrService;
import com.petadoption.petadoption.service.ActivityLogService;
import com.petadoption.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final OcrService ocrService;
    private final ActivityLogService activityLogService;

    @Override
    public User register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setUserType(request.getUserType() != null ? request.getUserType() : "PERSONAL");
        user.setRole("SHELTER".equals(request.getUserType()) ? "SHELTER" : "USER");
        user.setStatus(1);
        user.setCreditScore(100);
        user.setIsVerified(0);

        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被停用或锁定");
        }

        // 兼容明文密码和加密密码（仅用于开发环境）
        boolean passwordMatches = false;

        // 如果是 BCrypt 加密密码（以 $2a$ 开头）
        if (user.getPassword().startsWith("$2a$")) {
            passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            // 如果是明文密码（开发环境临时使用）
            passwordMatches = request.getPassword().equals(user.getPassword());
        }

        if (!passwordMatches) {
            throw new BusinessException("密码错误");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        updateLastLoginTime(user.getId());

        return token;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        User user = getById(userId);
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void updateCreditScore(Long userId, int delta) {
        User user = getById(userId);
        int newScore = user.getCreditScore() + delta;
        user.setCreditScore(Math.max(0, Math.min(100, newScore)));
        updateById(user);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<User> getUserList(int page, int size, String keyword, String userType, Integer status, String startTime, String endTime) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> userPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (userType != null && !userType.isEmpty()) {
            if ("ADMIN".equals(userType)) {
                wrapper.eq(User::getRole, "ADMIN");
            } else if ("PERSONAL".equals(userType)) {
                wrapper.eq(User::getUserType, "PERSONAL").ne(User::getRole, "ADMIN");
            } else {
                wrapper.eq(User::getUserType, userType);
            }
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(User::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(User::getCreateTime, endTime);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(userPage, wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, int status) {
        User user = getById(userId);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public void updateUserTags(Long userId, String tags) {
        User user = getById(userId);
        user.setTags(tags);
        updateById(user);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.existsByPhone(phone);
    }

    @Override
    public User submitVerification(Long userId, String realName, String idFront, String idBack) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException(ResponseCode.VERIFICATION_ALREADY_SUBMITTED);
        }
        
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        if (idFront == null || idFront.trim().isEmpty()) {
            throw new BusinessException("请上传身份证正面图片");
        }
        
        if (idBack == null || idBack.trim().isEmpty()) {
            throw new BusinessException("请上传身份证反面图片");
        }
        
        String recognizedIdCard = null;
        
        if (idFront != null && !idFront.isEmpty()) {
            java.util.Map<String, String> frontResult = ocrService.recognizeIdCard(idFront);
            if ("true".equals(frontResult.get("success"))) {
                recognizedIdCard = frontResult.get("idCardNumber");
            } else {
                throw new BusinessException(frontResult.get("message"));
            }
        }
        
        if (recognizedIdCard == null && idBack != null && !idBack.isEmpty()) {
            java.util.Map<String, String> backResult = ocrService.recognizeIdCard(idBack);
            if ("true".equals(backResult.get("success"))) {
                recognizedIdCard = backResult.get("idCardNumber");
            } else {
                throw new BusinessException(backResult.get("message"));
            }
        }
        
        if (recognizedIdCard == null) {
            throw new BusinessException(ResponseCode.ID_CARD_RECOGNITION_FAILED);
        }
        
        user.setRealName(realName.trim());
        user.setIdCard(recognizedIdCard);
        user.setIsVerified(1);
        
        updateById(user);
        return user;
    }

    @Override
    public User approveVerification(Long userId, boolean approved) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        user.setIsVerified(approved ? 1 : -1);
        updateById(user);
        return user;
    }

    @Override
    public User submitManualVerification(Long userId, String realName, String idCard) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException(ResponseCode.VERIFICATION_ALREADY_SUBMITTED);
        }
        
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        if (idCard == null || idCard.trim().isEmpty()) {
            throw new BusinessException("身份证号码不能为空");
        }
        
        String idCardTrimmed = idCard.trim().toUpperCase();
        if (!ocrService.validateIdCardNumber(idCardTrimmed)) {
            throw new BusinessException(ResponseCode.ID_CARD_INVALID);
        }
        
        user.setRealName(realName.trim());
        user.setIdCard(idCardTrimmed);
        user.setIsVerified(1);
        
        updateById(user);
        
        // 记录活动日志
        activityLogService.logActivity(userId, "verification", "user", userId, "完成实名认证");
        
        return user;
    }
}
