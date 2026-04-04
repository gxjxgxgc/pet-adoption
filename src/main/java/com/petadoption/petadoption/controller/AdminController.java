package com.petadoption.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetAdoption;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.service.AdoptionService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import com.petadoption.petadoption.service.CommentService;
import com.petadoption.petadoption.entity.PetComment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.lang.management.ManagementFactory;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员相关接口")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdoptionService adoptionService;
    private final PetService petService;
    private final CommentService commentService;

    private static final Map<String, Object> systemSettings = new HashMap<>();
    static {
        systemSettings.put("siteName", "宠物领养系统");
        systemSettings.put("siteDescription", "一个温暖的家，等待每一个小生命的到来");
        systemSettings.put("contactEmail", "support@petadoption.com");
        systemSettings.put("contactPhone", "");
        systemSettings.put("uploadPath", "C:/Users/user/Desktop/IDEA/pet-adoption/uploads");
        systemSettings.put("maxFileSize", 5);
        systemSettings.put("maxFileCount", 5);
        systemSettings.put("allowRegister", true);
        systemSettings.put("requireVerification", false);
        systemSettings.put("jwtExpiration", 86400000L);
        systemSettings.put("minPasswordLength", 6);
        systemSettings.put("reviewMode", "manual");
        systemSettings.put("applyValidDays", 7);
        systemSettings.put("maxApplyPerUser", 1);
        systemSettings.put("requireRating", false);
    }

    public static Object getSetting(String key) {
        return systemSettings.get(key);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取管理员统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getAdminStats() {
        long userCount = userService.count();
        long petCount = petService.count();
        long adoptionCount = adoptionService.count();
        long pendingCount = adoptionService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PetAdoption>()
                .eq(PetAdoption::getStatus, 0)
        );

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("userCount", userCount);
        stats.put("petCount", petCount);
        stats.put("adoptionCount", adoptionCount);
        stats.put("pendingCount", pendingCount);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/statistics/distribution")
    @Operation(summary = "获取数据分布统计", description = "获取宠物类型分布和领养状态分布")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDistribution() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> petTypeDist = new ArrayList<>();
        long catCount = petService.count(new LambdaQueryWrapper<Pet>().eq(Pet::getType, "cat"));
        long dogCount = petService.count(new LambdaQueryWrapper<Pet>().eq(Pet::getType, "dog"));
        long otherCount = petService.count(new LambdaQueryWrapper<Pet>().ne(Pet::getType, "cat").ne(Pet::getType, "dog"));

        Map<String, Object> catItem = new HashMap<>(); catItem.put("name", "猫咪"); catItem.put("value", catCount);
        Map<String, Object> dogItem = new HashMap<>(); dogItem.put("name", "狗狗"); dogItem.put("value", dogCount);
        Map<String, Object> otherItem = new HashMap<>(); otherItem.put("name", "其他"); otherItem.put("value", otherCount);

        if (catCount > 0) petTypeDist.add(catItem);
        if (dogCount > 0) petTypeDist.add(dogItem);
        if (otherCount > 0) petTypeDist.add(otherItem);
        result.put("petTypeDistribution", petTypeDist);

        List<Map<String, Object>> adoptionStatusDist = new ArrayList<>();
        String[] statusNames = {"待审核", "已通过", "已拒绝", "已取消", "已完成"};
        int[] statusValues = {0, 1, 2, 3, 4};
        for (int i = 0; i < statusValues.length; i++) {
            long cnt = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, statusValues[i]));
            if (cnt > 0 || true) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", statusNames[i]);
                item.put("value", cnt);
                adoptionStatusDist.add(item);
            }
        }
        result.put("adoptionStatusDistribution", adoptionStatusDist);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private final java.util.List<SseEmitter> sseEmitters = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile String lastDataHash = "";

    private String buildStatsHash(long uC, long pC, long aC, long pend,
            long catC, long dogC, long otherC, long s0, long s1, long s2, long s3, long s4,
            List<Map<String, Object>> adt, List<Map<String, Object>> ut,
            List<Map<String, Object>> apt, List<Map<String, Object>> cpt) {
        StringBuilder sb = new StringBuilder();
        sb.append(uC).append(":").append(pC).append(":").append(aC).append(":").append(pend);
        sb.append(":").append(catC).append(":").append(dogC).append(":").append(otherC);
        sb.append(":").append(s0).append(":").append(s1).append(":").append(s2).append(":").append(s3).append(":").append(s4);
        for (Map<String, Object> d : adt) { sb.append(d.get("count")); }
        for (Map<String, Object> d : ut) { sb.append(d.get("count")); }
        for (Map<String, Object> d : apt) { sb.append(d.get("count")); }
        for (Map<String, Object> d : cpt) { sb.append(d.get("count")); }
        return sb.toString();
    }

    private List<Map<String, Object>> buildDailyTrends(String tableName, String dateColumn) {
        return buildDailyTrends(tableName, dateColumn, -1);
    }

    private List<Map<String, Object>> buildDailyTrends(String tableName, String dateColumn, int status) {
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> day = new HashMap<>();
            day.put("date", d.toString());
            if ("pet_adoption".equals(tableName)) {
                LambdaQueryWrapper<PetAdoption> qw = new LambdaQueryWrapper<PetAdoption>();
                if (status >= 0) {
                    if (status == 1) {
                        qw.in(PetAdoption::getStatus, 1, 4);
                    } else {
                        qw.eq(PetAdoption::getStatus, status);
                    }
                    qw.ge(PetAdoption::getReviewTime, d.atStartOfDay())
                      .lt(PetAdoption::getReviewTime, d.plusDays(1).atStartOfDay());
                } else {
                    qw.ge(PetAdoption::getCreateTime, d.atStartOfDay())
                      .lt(PetAdoption::getCreateTime, d.plusDays(1).atStartOfDay());
                }
                day.put("count", adoptionService.count(qw));
            } else {
                day.put("count", userService.count(
                    new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, d.atStartOfDay())
                        .lt(User::getCreateTime, d.plusDays(1).atStartOfDay())));
            }
            trends.add(day);
        }
        return trends;
    }

    @GetMapping(value = "/statistics/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE实时数据推送", description = "数据变化时实时推送")
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter streamStatistics() {
        SseEmitter emitter = new SseEmitter(0L);
        sseEmitters.add(emitter);

        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        emitter.onError(e -> sseEmitters.remove(emitter));

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                boolean firstSend = true;
                while (true) {
                    long uC = userService.count();
                    long pC = petService.count();
                    long aC = adoptionService.count();
                    long pend = adoptionService.count(
                        new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 0));

                    long catC = petService.count(new LambdaQueryWrapper<Pet>().eq(Pet::getType, "cat"));
                    long dogC = petService.count(new LambdaQueryWrapper<Pet>().eq(Pet::getType, "dog"));
                    long otherC = petService.count(new LambdaQueryWrapper<Pet>().ne(Pet::getType, "cat").ne(Pet::getType, "dog"));

                    long s0 = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 0));
                    long s1 = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 1));
                    long s2 = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 2));
                    long s3 = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 3));
                    long s4 = adoptionService.count(new LambdaQueryWrapper<PetAdoption>().eq(PetAdoption::getStatus, 4));

                    List<Map<String, Object>> adt = buildDailyTrends("pet_adoption", "create_time");
                    List<Map<String, Object>> ut = buildDailyTrends("pet_user", "create_time");
                    List<Map<String, Object>> apt = buildDailyTrends("pet_adoption", "create_time", 1);
                    List<Map<String, Object>> cpt = buildDailyTrends("pet_adoption", "create_time", 3);

                    String currentHash = buildStatsHash(uC, pC, aC, pend, catC, dogC, otherC, s0, s1, s2, s3, s4, adt, ut, apt, cpt);

                    if (firstSend || !currentHash.equals(lastDataHash)) {
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("userCount", uC);
                        stats.put("petCount", pC);
                        stats.put("adoptionCount", aC);
                        stats.put("pendingCount", pend);

                        Map<String, Object> dist = new HashMap<>();
                        List<Map<String, Object>> petTypeDist = new ArrayList<>();
                        if (catC > 0) { Map<String, Object> m = new HashMap<>(); m.put("name","猫咪"); m.put("value",catC); petTypeDist.add(m); }
                        if (dogC > 0) { Map<String, Object> m = new HashMap<>(); m.put("name","狗狗"); m.put("value",dogC); petTypeDist.add(m); }
                        if (otherC > 0) { Map<String, Object> m = new HashMap<>(); m.put("name","其他"); m.put("value",otherC); petTypeDist.add(m); }
                        dist.put("petTypeDistribution", petTypeDist);

                        List<Map<String, Object>> statusDist = new ArrayList<>();
                        String[] sns = {"待审核","已通过","已拒绝","已取消","已完成"};
                        long[] sv = {s0, s1, s2, s3, s4};
                        for (int i = 0; i < 5; i++) {
                            Map<String, Object> m = new HashMap<>(); m.put("name",sns[i]); m.put("value",sv[i]); statusDist.add(m);
                        }
                        dist.put("adoptionStatusDistribution", statusDist);

                        Map<String, Object> payload = new HashMap<>();
                        payload.put("stats", stats);
                        payload.put("distribution", dist);
                        payload.put("adoptionTrends", adt);
                        payload.put("userTrends", ut);
                        payload.put("approvedTrends", apt);
                        payload.put("completedTrends", cpt);
                        payload.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        payload.put("changed", !firstSend);

                        emitter.send(SseEmitter.event().name("data").data(payload));
                        lastDataHash = currentHash;
                        firstSend = false;
                    }

                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户列表（管理员）", description = "获取所有用户列表，支持分页和筛选")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<User>>> getUserList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词(用户名/邮箱/手机号)") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户类型") @RequestParam(required = false) String userType,
            @Parameter(description = "状态(0禁用/1启用)") @RequestParam(required = false) Integer status,
            @Parameter(description = "注册开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "注册结束时间") @RequestParam(required = false) String endTime) {
        IPage<User> userPage = userService.getUserList(page, size, keyword, userType, status, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(userPage));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态（管理员）", description = "更新用户账号状态")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
            @Parameter(description = "状态更新数据") @RequestBody com.petadoption.petadoption.dto.StatusUpdateRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
    }

    @GetMapping("/users/pending-verifications")
    @Operation(summary = "获取待审核的实名认证列表（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getPendingVerifications() {
        List<User> users = userService.list(
            new LambdaQueryWrapper<User>()
                .eq(User::getIsVerified, 0)
        );
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{id}/verification")
    @Operation(summary = "审核实名认证（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> approveVerification(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "审核结果") @RequestBody Map<String, Boolean> request) {
        Boolean approved = request.get("approved");
        User user = userService.approveVerification(id, approved);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/adoptions/pending")
    @Operation(summary = "获取领养申请列表（管理员）", description = "支持状态筛选，默认显示全部非取消记录")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<Map<String, Object>>>> getPendingAdoptions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选：0-待审核 1-已通过 2-已拒绝 4-已完成，不传则全部") @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PetAdoption> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && status >= 0) {
            queryWrapper.eq(PetAdoption::getStatus, status);
        } else {
            queryWrapper.ne(PetAdoption::getStatus, 3);
        }
        queryWrapper.orderByDesc(PetAdoption::getCreateTime);
        IPage<PetAdoption> adoptionPage = adoptionService.page(new Page<>(page, size), queryWrapper);
        List<PetAdoption> records = adoptionPage.getRecords();

        Set<Long> petIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (PetAdoption a : records) {
            if (a.getPetId() != null) petIds.add(a.getPetId());
            if (a.getUserId() != null) userIds.add(a.getUserId());
        }

        Map<Long, Pet> petMap = new HashMap<>();
        if (!petIds.isEmpty()) {
            for (Pet p : petService.listByIds(petIds)) {
                petMap.put(p.getId(), p);
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userService.listByIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (PetAdoption a : records) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", a.getId());
            row.put("petId", a.getPetId());
            row.put("userId", a.getUserId());
            row.put("status", a.getStatus());
            row.put("reason", a.getReason());
            row.put("contactName", a.getContactName());
            row.put("phone", a.getPhone());
            row.put("address", a.getAddress());
            row.put("housingType", a.getHousingType());
            row.put("experience", a.getExperience());
            row.put("experienceDesc", a.getExperienceDesc());
            row.put("petPlan", a.getPetPlan());
            row.put("workInfo", a.getWorkInfo());
            row.put("familyInfo", a.getFamilyInfo());
            row.put("financialStatus", a.getFinancialStatus());
            row.put("rejectReason", a.getRejectReason());
            row.put("reviewComment", a.getReviewComment());
            row.put("applyTime", a.getApplyTime());
            row.put("reviewTime", a.getReviewTime());
            row.put("reviewerId", a.getReviewerId());
            row.put("completeTime", a.getCompleteTime());
            row.put("createTime", a.getCreateTime());

            Pet pet = petMap.get(a.getPetId());
            if (pet != null) {
                row.put("petName", pet.getName());
                row.put("petImages", pet.getImages());
                row.put("petType", pet.getType());
            } else {
                row.put("petName", "未知宠物");
                row.put("petImages", "");
                row.put("petType", "");
            }

            User user = userMap.get(a.getUserId());
            row.put("username", user != null ? user.getUsername() : "未知用户");

            enrichedRecords.add(row);
        }

        Page<Map<String, Object>> resultPage = new Page<>(adoptionPage.getCurrent(), adoptionPage.getSize(), adoptionPage.getTotal());
        resultPage.setRecords(enrichedRecords);
        return ResponseEntity.ok(ApiResponse.success(resultPage));
    }

    @PutMapping("/adoptions/{id}/review")
    @Operation(summary = "审核领养申请（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PetAdoption>> reviewAdoption(
            @Parameter(description = "领养ID") @PathVariable Long id,
            @RequestBody com.petadoption.petadoption.dto.AdoptionReviewRequest request) {
        PetAdoption adoption = adoptionService.reviewAdoption(id, request.getStatus(), request.getRejectReason(), null);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @GetMapping("/statistics/trends")
    @Operation(summary = "获取数据统计趋势", description = "获取指定时间范围内的统计数据趋势")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatisticsTrends(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "数据类型：adoption/user/pet") @RequestParam(defaultValue = "adoption") String type) {
        
        Map<String, Object> result = new HashMap<>();
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        if ("adoption".equals(type) || "all".equals(type)) {
            List<Map<String, Object>> adoptionTrends = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.toString());
                
                long count = adoptionService.count(
                    new LambdaQueryWrapper<PetAdoption>()
                        .ge(PetAdoption::getCreateTime, current.atStartOfDay())
                        .lt(PetAdoption::getCreateTime, current.plusDays(1).atStartOfDay())
                );
                dayData.put("count", count);
                adoptionTrends.add(dayData);
                current = current.plusDays(1);
            }
            result.put("adoptionTrends", adoptionTrends);

            List<Map<String, Object>> approvedTrends = new ArrayList<>();
            current = start;
            while (!current.isAfter(end)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.toString());
                dayData.put("count", adoptionService.count(
                    new LambdaQueryWrapper<PetAdoption>()
                        .in(PetAdoption::getStatus, 1, 4)
                        .ge(PetAdoption::getReviewTime, current.atStartOfDay())
                        .lt(PetAdoption::getReviewTime, current.plusDays(1).atStartOfDay())));
                approvedTrends.add(dayData);
                current = current.plusDays(1);
            }
            result.put("approvedTrends", approvedTrends);
        }
        
        if ("user".equals(type) || "all".equals(type)) {
            List<Map<String, Object>> userTrends = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.toString());
                
                long count = userService.count(
                    new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, current.atStartOfDay())
                        .lt(User::getCreateTime, current.plusDays(1).atStartOfDay())
                );
                dayData.put("count", count);
                userTrends.add(dayData);
                current = current.plusDays(1);
            }
            result.put("userTrends", userTrends);
        }
        
        if ("pet".equals(type) || "all".equals(type)) {
            com.petadoption.petadoption.entity.Pet pet = new com.petadoption.petadoption.entity.Pet();
            List<Map<String, Object>> petTrends = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", current.toString());
                
                long count = petService.count(
                    new LambdaQueryWrapper<com.petadoption.petadoption.entity.Pet>()
                        .ge(com.petadoption.petadoption.entity.Pet::getCreateTime, current.atStartOfDay())
                        .lt(com.petadoption.petadoption.entity.Pet::getCreateTime, current.plusDays(1).atStartOfDay())
                );
                dayData.put("count", count);
                petTrends.add(dayData);
                current = current.plusDays(1);
            }
            result.put("petTrends", petTrends);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/statistics/summary")
    @Operation(summary = "获取数据统计摘要", description = "获取各维度的统计汇总数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalUsers", userService.count());
        summary.put("totalPets", petService.count());
        summary.put("totalAdoptions", adoptionService.count());
        
        long verifiedUsers = userService.count(
            new LambdaQueryWrapper<User>()
                .isNotNull(User::getRealName)
                .ne(User::getRealName, "")
        );
        summary.put("verifiedUsers", verifiedUsers);
        
        long adoptedPets = petService.count(
            new LambdaQueryWrapper<com.petadoption.petadoption.entity.Pet>()
                .eq(com.petadoption.petadoption.entity.Pet::getStatus, 3)
        );
        summary.put("adoptedPets", adoptedPets);
        
        long pendingAdoptions = adoptionService.count(
            new LambdaQueryWrapper<PetAdoption>()
                .eq(PetAdoption::getStatus, 0)
        );
        summary.put("pendingAdoptions", pendingAdoptions);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/comments")
    @Operation(summary = "获取评论列表（管理员）", description = "分页获取评论列表，支持关键词和状态筛选")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommentList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态：0-待审核，1-已发布") @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PetComment> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(PetComment::getContent, keyword);
        }
        if (status != null) {
            wrapper.eq(PetComment::getStatus, status);
        }
        wrapper.orderByDesc(PetComment::getCreateTime);
        IPage<PetComment> pageResult = commentService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> petIds = new HashSet<>();
        for (PetComment comment : pageResult.getRecords()) {
            if (comment.getUserId() != null) userIds.add(comment.getUserId());
            if (comment.getPetId() != null) petIds.add(comment.getPetId());
        }

        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Long uid : userIds) {
                User u = userService.getById(uid);
                if (u != null) userMap.put(uid, u.getUsername());
            }
        }

        Map<Long, String> petMap = new HashMap<>();
        if (!petIds.isEmpty()) {
            for (Long pid : petIds) {
                var p = petService.getById(pid);
                if (p != null) petMap.put(pid, p.getName());
            }
        }

        for (PetComment comment : pageResult.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", comment.getId());
            row.put("userId", comment.getUserId());
            row.put("petId", comment.getPetId());
            row.put("content", comment.getContent());
            row.put("likeCount", comment.getLikeCount());
            row.put("status", comment.getStatus());
            row.put("createTime", comment.getCreateTime());
            row.put("updateTime", comment.getUpdateTime());
            row.put("username", userMap.getOrDefault(comment.getUserId(), "未知用户"));
            row.put("petName", petMap.getOrDefault(comment.getPetId(), "未知宠物"));
            enrichedRecords.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", enrichedRecords);
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        result.put("pages", pageResult.getPages());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/comments/{id}/status")
    @Operation(summary = "更新评论状态（管理员）", description="通过或隐藏评论")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateCommentStatus(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        PetComment comment = commentService.getById(id);
        if (comment == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("评论不存在"));
        }
        comment.setStatus(request.get("status"));
        commentService.updateById(comment);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.removeById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }

    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("environment", "Production");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("database", "MySQL 8.0");
        info.put("serverPort", 8082);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        info.put("startTime", LocalDateTime.now().format(formatter));
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long hours = uptime / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        info.put("upTime", hours + "h " + minutes + "m");
        Runtime runtime = Runtime.getRuntime();
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMB = runtime.maxMemory() / (1024 * 1024);
        info.put("memoryUsage", usedMB + "MB / " + maxMB + "MB");
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @GetMapping("/settings")
    @Operation(summary = "获取系统设置")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        Map<String, Object> basic = new HashMap<>();
        basic.put("siteName", systemSettings.getOrDefault("siteName", "宠物领养系统"));
        basic.put("siteDescription", systemSettings.getOrDefault("siteDescription", "一个温暖的家，等待每一个小生命的到来"));
        basic.put("contactEmail", systemSettings.getOrDefault("contactEmail", "support@petadoption.com"));
        basic.put("contactPhone", systemSettings.getOrDefault("contactPhone", ""));
        settings.put("basic", basic);

        Map<String, Object> upload = new HashMap<>();
        upload.put("uploadPath", systemSettings.getOrDefault("uploadPath", "C:/Users/user/Desktop/IDEA/pet-adoption/uploads"));
        upload.put("maxFileSize", systemSettings.getOrDefault("maxFileSize", 5));
        upload.put("maxFileCount", systemSettings.getOrDefault("maxFileCount", 5));
        settings.put("upload", upload);

        Map<String, Object> security = new HashMap<>();
        security.put("allowRegister", systemSettings.getOrDefault("allowRegister", true));
        security.put("requireVerification", systemSettings.getOrDefault("requireVerification", false));
        security.put("jwtExpiration", systemSettings.getOrDefault("jwtExpiration", 86400000L));
        security.put("minPasswordLength", systemSettings.getOrDefault("minPasswordLength", 6));
        settings.put("security", security);

        Map<String, Object> adoption = new HashMap<>();
        adoption.put("reviewMode", systemSettings.getOrDefault("reviewMode", "manual"));
        adoption.put("applyValidDays", systemSettings.getOrDefault("applyValidDays", 7));
        adoption.put("maxApplyPerUser", systemSettings.getOrDefault("maxApplyPerUser", 1));
        adoption.put("requireRating", systemSettings.getOrDefault("requireRating", false));
        settings.put("adoption", adoption);

        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings/basic")
    @Operation(summary = "保存基本设置")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> saveBasicSettings(@RequestBody Map<String, String> settings) {
        if (settings.containsKey("siteName")) systemSettings.put("siteName", settings.get("siteName"));
        if (settings.containsKey("siteDescription")) systemSettings.put("siteDescription", settings.get("siteDescription"));
        if (settings.containsKey("contactEmail")) systemSettings.put("contactEmail", settings.get("contactEmail"));
        if (settings.containsKey("contactPhone")) systemSettings.put("contactPhone", settings.get("contactPhone"));
        return ResponseEntity.ok(ApiResponse.success("基本设置保存成功"));
    }

    @PutMapping("/settings/upload")
    @Operation(summary = "保存上传设置")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> saveUploadSettings(@RequestBody Map<String, Object> settings) {
        if (settings.containsKey("maxFileSize")) systemSettings.put("maxFileSize", settings.get("maxFileSize"));
        if (settings.containsKey("maxFileCount")) systemSettings.put("maxFileCount", settings.get("maxFileCount"));
        return ResponseEntity.ok(ApiResponse.success("上传设置保存成功"));
    }

    @PutMapping("/settings/security")
    @Operation(summary = "保存安全设置")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> saveSecuritySettings(@RequestBody Map<String, Object> settings) {
        if (settings.containsKey("allowRegister")) systemSettings.put("allowRegister", settings.get("allowRegister"));
        if (settings.containsKey("requireVerification")) systemSettings.put("requireVerification", settings.get("requireVerification"));
        if (settings.containsKey("jwtExpiration")) systemSettings.put("jwtExpiration", settings.get("jwtExpiration"));
        if (settings.containsKey("minPasswordLength")) systemSettings.put("minPasswordLength", settings.get("minPasswordLength"));
        return ResponseEntity.ok(ApiResponse.success("安全设置保存成功"));
    }

    @PutMapping("/settings/adoption")
    @Operation(summary = "保存领养规则设置")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> saveAdoptionSettings(@RequestBody Map<String, Object> settings) {
        if (settings.containsKey("reviewMode")) systemSettings.put("reviewMode", settings.get("reviewMode"));
        if (settings.containsKey("applyValidDays")) systemSettings.put("applyValidDays", settings.get("applyValidDays"));
        if (settings.containsKey("maxApplyPerUser")) systemSettings.put("maxApplyPerUser", settings.get("maxApplyPerUser"));
        if (settings.containsKey("requireRating")) systemSettings.put("requireRating", settings.get("requireRating"));
        return ResponseEntity.ok(ApiResponse.success("领养规则保存成功"));
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "清除系统缓存")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        lastDataHash = "";
        return ResponseEntity.ok(ApiResponse.success("系统缓存已清除"));
    }
}
