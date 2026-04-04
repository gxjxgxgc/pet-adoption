package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.dto.LoginRequest;
import com.petadoption.petadoption.dto.RegisterRequest;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.entity.ActivityLog;
import com.petadoption.petadoption.entity.PetFavorite;
import com.petadoption.petadoption.entity.PetAdoption;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.UserService;
import com.petadoption.petadoption.service.ActivityLogService;
import com.petadoption.petadoption.service.FavoriteService;
import com.petadoption.petadoption.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息查询等接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ActivityLogService activityLogService;
    private final FavoriteService favoriteService;
    private final AdoptionService adoptionService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ResponseEntity<ApiResponse<User>> register(@Validated @RequestBody RegisterRequest request) {
        Object allowRegister = AdminController.getSetting("allowRegister");
        if (allowRegister != null && !Boolean.TRUE.equals(allowRegister)) {
            return ResponseEntity.ok(ApiResponse.error(403, "系统已关闭注册功能，请联系管理员"));
        }
        Object minLenObj = AdminController.getSetting("minPasswordLength");
        int minLen = (minLenObj instanceof Number) ? ((Number) minLenObj).intValue() : 6;
        if (request.getPassword() != null && request.getPassword().length() < minLen) {
            return ResponseEntity.ok(ApiResponse.error(400, "密码长度不能少于" + minLen + "个字符"));
        }
        User user = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    public ResponseEntity<ApiResponse<Object>> login(@Validated @RequestBody LoginRequest request) {
        String token = userService.login(request);
        User user = userService.getUserByUsername(request.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("role", user.getRole());
        data.put("userType", user.getUserType());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录，清除认证状态")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新当前用户资料", description = "更新当前登录用户的个人资料")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestBody User userUpdate,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }

        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.USER_NOT_FOUND));
        }

        String oldPassword = userUpdate.getOldPassword();
        String newPassword = userUpdate.getNewPassword();
        
        System.out.println("=== Password Update Debug ===");
        System.out.println("oldPassword from request: " + (oldPassword != null ? "present" : "null"));
        System.out.println("newPassword from request: " + (newPassword != null ? "present" : "null"));
        System.out.println("existingUser password hash: " + existingUser.getPassword());
        
        if (oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            boolean passwordMatches = passwordEncoder.matches(oldPassword, existingUser.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (!passwordMatches) {
                return ResponseEntity.ok(ApiResponse.error(ResponseCode.PASSWORD_ERROR));
            }
            
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            System.out.println("New encoded password: " + encodedNewPassword);
            existingUser.setPassword(encodedNewPassword);
            userService.updateById(existingUser);
            System.out.println("Password updated successfully");
            return ResponseEntity.ok(ApiResponse.success(existingUser));
        }

        boolean hasChanges = false;

        String newEmail = userUpdate.getEmail();
        String newPhone = userUpdate.getPhone();
        String newRealName = userUpdate.getRealName();
        String newAvatar = userUpdate.getAvatar();
        String newGender = userUpdate.getGender();
        Integer newAge = userUpdate.getAge();
        String newAddress = userUpdate.getAddress();
        String newBio = userUpdate.getBio();
        LocalDate newBirthday = userUpdate.getBirthday();

        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(existingUser.getEmail())) {
            existingUser.setEmail(newEmail);
            hasChanges = true;
        }
        if (newPhone != null && !newPhone.isEmpty() && !newPhone.equals(existingUser.getPhone())) {
            if (userService.existsByPhone(newPhone)) {
                return ResponseEntity.ok(ApiResponse.error(ResponseCode.USER_PHONE_EXISTS));
            }
            existingUser.setPhone(newPhone);
            hasChanges = true;
        }
        if (newRealName != null && !newRealName.equals(existingUser.getRealName())) {
            existingUser.setRealName(newRealName);
            hasChanges = true;
        }
        
        String newUsername = userUpdate.getUsername();
        if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(existingUser.getUsername())) {
            if (newUsername.length() < 3 || newUsername.length() > 20) {
                return ResponseEntity.ok(ApiResponse.error(400, "用户名长度需在3-20个字符之间"));
            }
            if (!newUsername.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
                return ResponseEntity.ok(ApiResponse.error(400, "用户名只能包含字母、数字、下划线和中文"));
            }
            if (userService.getUserByUsername(newUsername) != null) {
                return ResponseEntity.ok(ApiResponse.error(ResponseCode.USER_ALREADY_EXISTS));
            }
            existingUser.setUsername(newUsername);
            hasChanges = true;
        }
        
        if (newAvatar != null && !newAvatar.equals(existingUser.getAvatar())) {
            existingUser.setAvatar(newAvatar);
            hasChanges = true;
        }
        if (newGender != null && !newGender.equals(existingUser.getGender())) {
            existingUser.setGender(newGender);
            hasChanges = true;
        }
        if (newAge != null && !newAge.equals(existingUser.getAge())) {
            existingUser.setAge(newAge);
            hasChanges = true;
        }
        if (newAddress != null && !newAddress.equals(existingUser.getAddress())) {
            existingUser.setAddress(newAddress);
            hasChanges = true;
        }
        if (newBio != null && !newBio.equals(existingUser.getBio())) {
            existingUser.setBio(newBio);
            hasChanges = true;
        }
        if (newBirthday != null && !newBirthday.equals(existingUser.getBirthday())) {
            existingUser.setBirthday(newBirthday);
            hasChanges = true;
        }

        if (hasChanges) {
            userService.updateById(existingUser);
        }
        return ResponseEntity.ok(ApiResponse.success(existingUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户 ID 获取详细信息")
    public ResponseEntity<ApiResponse<User>> getUserInfo(
            @Parameter(description = "用户 ID", example = "1")
            @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.USER_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/verification")
    @Operation(summary = "提交实名认证", description = "用户提交实名认证信息，系统将自动识别身份证号码")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> submitVerification(
            @RequestBody Map<String, String> verificationData,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }

        String realName = verificationData.get("realName");
        String idFront = verificationData.get("idFront");
        String idBack = verificationData.get("idBack");

        if (realName == null || realName.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PARAM_ERROR));
        }
        if (idFront == null || idFront.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PARAM_ERROR));
        }
        if (idBack == null || idBack.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PARAM_ERROR));
        }

        User user = userService.submitVerification(userId, realName, idFront, idBack);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/verification/manual")
    @Operation(summary = "手动提交实名认证", description = "用户手动输入身份证信息完成实名认证")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> submitManualVerification(
            @RequestBody Map<String, String> verificationData,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }

        String realName = verificationData.get("realName");
        String idCard = verificationData.get("idCard");

        if (realName == null || realName.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PARAM_ERROR));
        }
        if (idCard == null || idCard.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PARAM_ERROR));
        }

        User user = userService.submitManualVerification(userId, realName, idCard);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "自动从 Token 获取当前登录用户信息（无需传参）")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        // 从 Spring Security 上下文自动获取当前登录用户
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取当前用户实时统计", description = "返回收藏数、领养完成数、信用分等实时统计数据")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(
            @RequestHeader("Authorization") String authorization) {
        Long userId = getCurrentUserId(authorization);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }

        long favoriteCount = favoriteService.count(
            new LambdaQueryWrapper<PetFavorite>()
                .eq(PetFavorite::getUserId, userId));
        long adoptionCompletedCount = adoptionService.count(
            new LambdaQueryWrapper<PetAdoption>()
                .eq(PetAdoption::getUserId, userId)
                .in(PetAdoption::getStatus, 1, 4));
        long pendingAdoptionCount = adoptionService.count(
            new LambdaQueryWrapper<PetAdoption>()
                .eq(PetAdoption::getUserId, userId)
                .eq(PetAdoption::getStatus, 0));
        User user = userService.getById(userId);
        int creditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("favoriteCount", favoriteCount);
        stats.put("adoptionCompletedCount", adoptionCompletedCount);
        stats.put("pendingAdoptionCount", pendingAdoptionCount);
        stats.put("creditScore", creditScore);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/admin/users")
    @Operation(summary = "获取用户列表（管理员）", description = "获取所有用户列表，支持分页和筛选")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<User>>> getUserList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词", example = "admin") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户类型", example = "ADMIN") @RequestParam(required = false) String userType,
            @Parameter(description = "状态", example = "1") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间", example = "2024-01-01") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2024-12-31") @RequestParam(required = false) String endTime) {
        com.baomidou.mybatisplus.core.metadata.IPage<User> userPage = userService.getUserList(page, size, keyword, userType, status, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(userPage));
    }

    @PutMapping("/admin/users/{id}/status")
    @Operation(summary = "更新用户状态（管理员）", description = "更新用户账号状态")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
            @Parameter(description = "状态更新数据") @RequestBody com.petadoption.petadoption.dto.StatusUpdateRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
    }

    @PutMapping("/admin/users/{id}/tags")
    @Operation(summary = "更新用户标签（管理员）", description = "更新用户标签")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUserTags(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
            @Parameter(description = "标签更新数据") @RequestBody Map<String, String> request) {
        userService.updateUserTags(id, request.get("tags"));
        return ResponseEntity.ok(ApiResponse.success("标签更新成功"));
    }

    @GetMapping("/admin/export")
    @Operation(summary = "导出用户数据（管理员）", description = "导出用户数据为Excel文件")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<User> users = userService.list();
            
            Workbook workbook = new XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("用户数据");
            
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "用户名", "邮箱", "手机号", "用户类型", "角色", "状态", "实名认证", "注册时间", "最后登录"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(user.getId() != null ? user.getId() : 0);
                row.createCell(1).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(4).setCellValue(user.getUserType() != null ? user.getUserType() : "");
                row.createCell(5).setCellValue(user.getRole() != null ? user.getRole() : "");
                row.createCell(6).setCellValue(user.getStatus() != null && user.getStatus() == 1 ? "正常" : "禁用");
                row.createCell(7).setCellValue(user.getRealName() != null && !user.getRealName().isEmpty() ? "已认证" : "未认证");
                row.createCell(8).setCellValue(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
                row.createCell(9).setCellValue(user.getUpdateTime() != null ? user.getUpdateTime().toString() : "");
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=users.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getCurrentUserId(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUserId(token);
        User user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    @GetMapping("/activities")
    @Operation(summary = "获取用户活动记录", description = "获取当前用户的活动日志列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ActivityLog>>> getUserActivities(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String activityType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Long userId = getCurrentUserId(authorization);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.UNAUTHORIZED));
        }
        
        List<ActivityLog> activities = activityLogService.getUserActivities(userId, activityType, page, size);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
