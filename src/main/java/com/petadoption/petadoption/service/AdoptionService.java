package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.PetAdoption;

import java.util.List;

public interface AdoptionService extends IService<PetAdoption> {

    /**
     * 申请领养
     */
    PetAdoption applyForAdoption(Long petId, Long applicantId, String message);

    /**
     * 申请领养（详细信息）
     */
    PetAdoption applyForAdoptionWithDetails(Long petId, Long applicantId, String reason, String contactName, String phone, String address, String housingType, Integer experience, String experienceDesc, String petPlan, String workInfo, String familyInfo, String financialStatus);

    /**
     * 审核领养
     */
    PetAdoption auditAdoption(Long adoptionId, Integer status, String reason, Long auditorId);

    /**
     * 获取用户的领养记录
     */
    List<PetAdoption> getUserAdoptions(Long userId);

    /**
     * 获取用户指定状态的领养记录
     */
    List<PetAdoption> getUserAdoptionsByStatus(Long userId, Integer status);

    /**
     * 获取宠物的领养记录
     */
    List<PetAdoption> getPetAdoptions(Long petId);

    /**
     * 获取待审核的领养申请（分页）
     */
    IPage<PetAdoption> getPendingAdoptions(int page, int size);

    /**
     * 审核领养申请
     */
    PetAdoption reviewAdoption(Long adoptionId, Integer status, String rejectReason, Long auditorId);

    /**
     * 确认领养
     */
    PetAdoption confirmAdoption(Long adoptionId, Long userId);

    /**
     * 评价领养
     */
    PetAdoption rateAdoption(Long adoptionId, Long userId, Integer rating, String comment);

    /**
     * 取消领养申请
     */
    void cancelAdoption(Long adoptionId, Long userId);
}
