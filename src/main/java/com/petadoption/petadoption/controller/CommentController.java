package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.entity.PetComment;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.CommentService;
import com.petadoption.petadoption.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@Tag(name = "评论管理", description = "宠物评论相关接口")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "发表评论")
    public ResponseEntity<ApiResponse<PetComment>> createComment(
            @Validated @RequestBody PetComment comment,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        PetComment savedComment = commentService.addComment(
            userId,
            comment.getPetId(),
            comment.getContent(),
            comment.getParentId()
        );

        return ResponseEntity.ok(ApiResponse.success(savedComment));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "评论 ID") @PathVariable Long commentId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/pet/{petId}")
    @Operation(summary = "获取宠物的评论列表")
    public ResponseEntity<ApiResponse<List<PetComment>>> getPetComments(
            @Parameter(description = "宠物 ID") @PathVariable Long petId) {

        List<PetComment> comments = commentService.getPetComments(petId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    private Long getCurrentUserId(String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.extractUserId(token);
        User user = userService.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}
