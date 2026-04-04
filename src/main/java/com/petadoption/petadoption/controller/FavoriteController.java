package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetFavorite;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.FavoriteService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@Tag(name = "收藏管理", description = "宠物收藏相关接口")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PetService petService;

    @PostMapping("/{petId}")
    @Operation(summary = "收藏宠物")
    public ResponseEntity<ApiResponse<Map<String, Object>>> favorite(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        favoriteService.favorite(userId, petId);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", true);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{petId}")
    @Operation(summary = "取消收藏")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unfavorite(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        favoriteService.unfavorite(userId, petId);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", false);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{petId}/check")
    @Operation(summary = "检查收藏状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFavorite(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        boolean favorited = favoriteService.isFavorited(userId, petId);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{petId}/toggle")
    @Operation(summary = "切换收藏状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFavorite(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        boolean favorited = favoriteService.toggleFavorite(userId, petId);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "获取用户的收藏列表")
    public ResponseEntity<ApiResponse<List<PetFavorite>>> getUserFavorites(
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        List<PetFavorite> favorites = favoriteService.getUserFavorites(userId, null, null, null);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户的收藏列表（分页）")
    public ResponseEntity<ApiResponse<Object>> getMyFavorites(
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "8") Integer size,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(required = false) String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false) String sortOrder,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        List<PetFavorite> favorites = favoriteService.getUserFavorites(userId, keyword, sortBy, sortOrder);
        
        int start = (page - 1) * size;
        int end = Math.min(start + size, favorites.size());
        
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        if (start < favorites.size()) {
            List<PetFavorite> pageList = favorites.subList(start, end);
            for (PetFavorite fav : pageList) {
                Pet pet = petService.getById(fav.getPetId());
                if (pet != null) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", fav.getPetId());
                    record.put("petId", fav.getPetId());
                    record.put("name", pet.getName());
                    record.put("type", pet.getType());
                    record.put("breed", pet.getBreed());
                    record.put("age", pet.getAge());
                    record.put("gender", pet.getGender());
                    record.put("color", pet.getColor());
                    record.put("weight", pet.getWeight());
                    record.put("description", pet.getDescription());
                    record.put("images", pet.getImages());
                    record.put("status", pet.getStatus());
                    record.put("createTime", fav.getCreateTime());
                    records.add(record);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", favorites.size());
        result.put("current", page);
        result.put("size", size);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量取消收藏")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUnfavorite(
            @RequestBody List<Long> petIds,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        favoriteService.batchUnfavorite(userId, petIds);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", petIds.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private Long getCurrentUserId(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUserId(token);
        User user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}
