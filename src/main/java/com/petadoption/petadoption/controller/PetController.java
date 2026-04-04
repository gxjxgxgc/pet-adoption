package com.petadoption.petadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.dto.PageResult;
import com.petadoption.petadoption.entity.HealthStatus;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.HealthStatusService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "宠物管理", description = "宠物信息查询、搜索等接口")
@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;
    private final HealthStatusService healthStatusService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    @Operation(summary = "获取宠物列表", description = "分页查询宠物列表，支持多条件筛选和排序")
    public ResponseEntity<ApiResponse<PageResult<Pet>>> getPets(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "关键词搜索（可选）", example = "猫")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "宠物类型（可选）", example = "cat")
            @RequestParam(required = false) String type,
            @Parameter(description = "品种（可选）", example = "金毛")
            @RequestParam(required = false) String breed,
            @Parameter(description = "性别（可选）", example = "1")
            @RequestParam(required = false) Integer gender,
            @Parameter(description = "最小年龄（月）", example = "0")
            @RequestParam(required = false) Integer minAge,
            @Parameter(description = "最大年龄（月）", example = "120")
            @RequestParam(required = false) Integer maxAge,
            @Parameter(description = "最小体重（kg）", example = "0")
            @RequestParam(required = false) Double minWeight,
            @Parameter(description = "最大体重（kg）", example = "50")
            @RequestParam(required = false) Double maxWeight,
            @Parameter(description = "健康状况（可选，多个值用逗号分隔）", example = "healthy,vaccinated")
            @RequestParam(required = false) String healthStatus,
            @Parameter(description = "排序字段（可选）：publishTime, age, weight, viewCount, favoriteCount", example = "publishTime")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "排序方向（可选）：asc, desc", example = "desc")
            @RequestParam(required = false) String sortOrder,
            @Parameter(description = "状态：0-隐藏，1-待领养，2-领养中，3-已领养", example = "1")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "状态列表（可选，多个用逗号分隔）", example = "1,2")
            @RequestParam(required = false) String statusListParam
    ) {
        Page<Pet> page = new Page<>(current, size);
        
        // 将逗号分隔的健康状况字符串转换为列表
        List<String> healthStatusList = null;
        if (healthStatus != null && !healthStatus.isEmpty()) {
            healthStatusList = new ArrayList<>(Arrays.asList(healthStatus.split(",")));
        }
        
        // 将逗号分隔的状态字符串转换为列表
        List<Integer> statusList = null;
        if (statusListParam != null && !statusListParam.isEmpty()) {
            statusList = new ArrayList<>();
            for (String s : statusListParam.split(",")) {
                try {
                    statusList.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        
        IPage<Pet> petPage = petService.getPetsByFilter(page, keyword, type, breed, gender, minAge, maxAge, minWeight, maxWeight, healthStatusList, sortBy, sortOrder, status, statusList);
        PageResult<Pet> result = PageResult.from(petPage);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情", description = "根据宠物 ID 获取详细信息")
    public ResponseEntity<ApiResponse<Pet>> getPetDetail(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id) {
        Pet pet = petService.getPetWithHealthStatuses(id);
        if (pet == null) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PET_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(pet));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索宠物", description = "根据关键词搜索宠物名称或品种")
    public ResponseEntity<ApiResponse<PageResult<Pet>>> searchPets(
            @Parameter(description = "搜索关键词", example = "金毛")
            @RequestParam String keyword,
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Pet> page = new Page<>(current, size);
        IPage<Pet> petPage = petService.searchPets(page, keyword);
        PageResult<Pet> result = PageResult.from(petPage);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @Operation(summary = "创建宠物", description = "创建新的宠物信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<Boolean>> createPet(
            @Parameter(description = "宠物信息")
            @RequestBody Pet pet,
            @Parameter(description = "授权令牌")
            @RequestHeader("Authorization") String authorization) {
        Long userId = getCurrentUserId(authorization);
        pet.setShelterId(userId);
        boolean result = petService.savePet(pet);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    private Long getCurrentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUserId(token);
        User user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物", description = "更新宠物信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<Boolean>> updatePet(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "宠物信息")
            @RequestBody Pet pet) {
        boolean result = petService.updatePet(id, pet);
        if (!result) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PET_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物", description = "删除宠物信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<Boolean>> deletePet(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id) {
        boolean result = petService.deletePet(id);
        if (!result) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PET_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新宠物状态", description = "更新宠物的状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<Boolean>> updatePetStatus(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "状态：0-隐藏，1-待领养，2-领养中，3-已领养", example = "1")
            @RequestParam Integer status) {
        boolean result = petService.updateStatus(id, status);
        if (!result) {
            return ResponseEntity.ok(ApiResponse.error(ResponseCode.PET_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门宠物", description = "获取热门宠物列表")
    public ResponseEntity<ApiResponse<PageResult<Pet>>> getHotPets(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Pet> page = new Page<>(current, size);
        IPage<Pet> petPage = petService.getHotPets(page);
        PageResult<Pet> result = PageResult.from(petPage);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/batch-upload")
    @Operation(summary = "批量上传宠物", description = "通过Excel文件批量导入宠物信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<String>> batchUploadPets(
            @Parameter(description = "Excel文件")
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        petService.batchImportPets(file);
        return ResponseEntity.ok(ApiResponse.success("批量上传成功"));
    }

    @GetMapping("/export")
    @Operation(summary = "导出宠物数据", description = "导出所有宠物数据为Excel文件")
    public void exportPets(jakarta.servlet.http.HttpServletResponse response) {
        try {
            petService.exportPets(response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @PutMapping("/{id}/view")
    @Operation(summary = "增加浏览量", description = "增加宠物的浏览量")
    public ResponseEntity<ApiResponse<Boolean>> incrementViewCount(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id) {
        petService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    @PutMapping("/{id}/favorite/increment")
    @Operation(summary = "增加收藏数", description = "增加宠物的收藏数")
    public ResponseEntity<ApiResponse<Boolean>> incrementFavoriteCount(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id) {
        petService.incrementFavoriteCount(id);
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    @PutMapping("/{id}/favorite/decrement")
    @Operation(summary = "减少收藏数", description = "减少宠物的收藏数")
    public ResponseEntity<ApiResponse<Boolean>> decrementFavoriteCount(
            @Parameter(description = "宠物 ID", example = "1")
            @PathVariable Long id) {
        petService.decrementFavoriteCount(id);
        return ResponseEntity.ok(ApiResponse.success(true));
    }
    
    @GetMapping("/health-statuses")
    @Operation(summary = "获取健康状况列表", description = "获取所有健康状况选项")
    public ResponseEntity<ApiResponse<List<HealthStatus>>> getHealthStatuses() {
        List<HealthStatus> healthStatuses = healthStatusService.list();
        return ResponseEntity.ok(ApiResponse.success(healthStatuses));
    }
}
