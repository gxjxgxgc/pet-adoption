package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetAdoption;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.security.JwtUtil;
import com.petadoption.petadoption.service.AdoptionService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;

@RestController
@RequestMapping("/adoptions")
@Tag(name = "领养管理", description = "领养申请相关接口")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PetService petService;

    @PostMapping("/{petId}/apply")
    @Operation(summary = "申请领养")
    public ResponseEntity<ApiResponse<PetAdoption>> applyAdoption(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Parameter(description = "申请理由") @RequestParam String reason,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.applyForAdoption(petId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @PostMapping("/{petId}/apply/details")
    @Operation(summary = "申请领养（详细版）")
    public ResponseEntity<ApiResponse<PetAdoption>> applyAdoptionWithDetails(
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Parameter(description = "申请留言") @RequestParam String message,
            @Parameter(description = "联系人") @RequestParam String contactName,
            @Parameter(description = "联系电话") @RequestParam String phone,
            @Parameter(description = "居住地址") @RequestParam String address,
            @Parameter(description = "住房类型") @RequestParam String housingType,
            @Parameter(description = "养宠经验") @RequestParam Integer experience,
            @Parameter(description = "经验描述") @RequestParam String experienceDesc,
            @Parameter(description = "养宠计划") @RequestParam String petPlan,
            @Parameter(description = "工作情况") @RequestParam String workInfo,
            @Parameter(description = "家庭情况") @RequestParam String familyInfo,
            @Parameter(description = "经济状况") @RequestParam String financialStatus,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.applyForAdoptionWithDetails(petId, userId, message, contactName, phone, address, housingType, experience, experienceDesc, petPlan, workInfo, familyInfo, financialStatus);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @PostMapping("/{id}/audit")
    @Operation(summary = "审核领养")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<PetAdoption>> auditAdoption(
            @Parameter(description = "领养 ID") @PathVariable Long id,
            @Parameter(description = "审核状态 0-待审核 1-通过 2-拒绝") @RequestParam Integer status,
            @Parameter(description = "审核意见") @RequestParam String reason,
            @RequestHeader("Authorization") String authorization) {

        Long auditorId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.auditAdoption(id, status, reason, auditorId);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户的领养记录")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyAdoptions(
            @Parameter(description = "状态筛选 0-待审核 1-通过 2-拒绝") @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        List<PetAdoption> adoptions;

        if (status != null) {
            adoptions = adoptionService.getUserAdoptionsByStatus(userId, status);
        } else {
            adoptions = adoptionService.getUserAdoptions(userId);
        }

        Set<Long> petIds = new HashSet<>();
        for (PetAdoption a : adoptions) {
            if (a.getPetId() != null) petIds.add(a.getPetId());
        }
        Map<Long, Pet> petMap = new HashMap<>();
        if (!petIds.isEmpty()) {
            for (Pet p : petService.listByIds(petIds)) {
                petMap.put(p.getId(), p);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (PetAdoption a : adoptions) {
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
                row.put("petBreed", pet.getBreed());
            } else {
                row.put("petName", "未知宠物");
                row.put("petImages", "");
                row.put("petType", "");
                row.put("petBreed", "");
            }
            result.add(row);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "获取领养记录列表")
    public ResponseEntity<ApiResponse<List<PetAdoption>>> getAdoptions(
            @RequestParam(required = false) Long petId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = getCurrentUserId(authorization);
        List<PetAdoption> adoptions;

        if (petId != null) {
            adoptions = adoptionService.getPetAdoptions(petId);
        } else {
            adoptions = adoptionService.getUserAdoptions(userId);
        }

        return ResponseEntity.ok(ApiResponse.success(adoptions));
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "审核领养申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER')")
    public ResponseEntity<ApiResponse<PetAdoption>> reviewAdoption(
            @Parameter(description = "领养ID") @PathVariable Long id,
            @RequestBody com.petadoption.petadoption.dto.AdoptionReviewRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long auditorId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.reviewAdoption(id, request.getStatus(), request.getRejectReason(), auditorId);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认领养")
    public ResponseEntity<ApiResponse<PetAdoption>> confirmAdoption(
            @Parameter(description = "领养ID") @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.confirmAdoption(id, userId);
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "评价领养")
    public ResponseEntity<ApiResponse<PetAdoption>> rateAdoption(
            @Parameter(description = "领养ID") @PathVariable Long id,
            @RequestBody @Validated com.petadoption.petadoption.dto.AdoptionRateRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getCurrentUserId(authorization);
        PetAdoption adoption = adoptionService.rateAdoption(id, userId, request.getRating(), request.getComment());
        return ResponseEntity.ok(ApiResponse.success(adoption));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消领养申请")
    public ResponseEntity<ApiResponse<String>> cancelAdoption(
            @Parameter(description = "领养ID") @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getCurrentUserId(authorization);
        adoptionService.cancelAdoption(id, userId);
        return ResponseEntity.ok(ApiResponse.success("取消申请成功"));
    }

    @GetMapping("/export")
    @Operation(summary = "导出领养申请数据（管理员）", description = "导出领养申请数据为Excel文件")
    public void exportAdoptions(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<PetAdoption> adoptions = adoptionService.list();
            
            Workbook workbook = new XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("领养申请数据");
            
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "宠物ID", "用户ID", "联系电话", "联系地址", "申请理由", "状态", "拒绝原因", "审核评论", "评分", "评价内容", "申请时间", "审核时间", "完成时间"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            for (int i = 0; i < adoptions.size(); i++) {
                PetAdoption adoption = adoptions.get(i);
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(adoption.getId() != null ? adoption.getId() : 0);
                row.createCell(1).setCellValue(adoption.getPetId() != null ? adoption.getPetId() : 0);
                row.createCell(2).setCellValue(adoption.getUserId() != null ? adoption.getUserId() : 0);
                row.createCell(3).setCellValue(adoption.getPhone() != null ? adoption.getPhone() : "");
                row.createCell(4).setCellValue(adoption.getAddress() != null ? adoption.getAddress() : "");
                row.createCell(5).setCellValue(adoption.getReason() != null ? adoption.getReason() : "");
                
                String statusText = "";
                if (adoption.getStatus() != null) {
                    switch (adoption.getStatus()) {
                        case 0: statusText = "待审核"; break;
                        case 1: statusText = "已通过"; break;
                        case 2: statusText = "已拒绝"; break;
                        case 3: statusText = "已取消"; break;
                        default: statusText = "未知";
                    }
                }
                row.createCell(6).setCellValue(statusText);
                row.createCell(7).setCellValue(adoption.getRejectReason() != null ? adoption.getRejectReason() : "");
                row.createCell(8).setCellValue(adoption.getReviewComment() != null ? adoption.getReviewComment() : "");
                row.createCell(9).setCellValue(adoption.getRating() != null ? adoption.getRating().doubleValue() : 0);
                row.createCell(10).setCellValue(adoption.getComment() != null ? adoption.getComment() : "");
                row.createCell(11).setCellValue(adoption.getApplyTime() != null ? adoption.getApplyTime().toString() : "");
                row.createCell(12).setCellValue(adoption.getReviewTime() != null ? adoption.getReviewTime().toString() : "");
                row.createCell(13).setCellValue(adoption.getCompleteTime() != null ? adoption.getCompleteTime().toString() : "");
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=adoptions.xlsx");
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
}
