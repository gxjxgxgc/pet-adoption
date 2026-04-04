package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetAdoption;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.PetAdoptionMapper;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.service.AdoptionService;
import com.petadoption.petadoption.service.ActivityLogService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl extends ServiceImpl<PetAdoptionMapper, PetAdoption> implements AdoptionService {
    
    private final PetService petService;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public PetAdoption applyForAdoption(Long petId, Long applicantId, String message) {
        Object requireVerif = com.petadoption.petadoption.controller.AdminController.getSetting("requireVerification");
        if (Boolean.TRUE.equals(requireVerif)) {
            User applicant = userService.getById(applicantId);
            if (applicant == null || applicant.getIsVerified() == null || applicant.getIsVerified() != 1) {
                throw new BusinessException("请先完成实名认证后再申请领养");
            }
        }

        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getPetId, petId)
               .eq(PetAdoption::getUserId, applicantId)
               .in(PetAdoption::getStatus, 0, 1);

        if (count(wrapper) > 0) {
            throw new BusinessException(ResponseCode.ADOPTION_STATUS_ERROR);
        }

        Pet pet = petService.getById(petId);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }

        if (pet.getStatus() == null || pet.getStatus() != 1) {
            throw new BusinessException("该宠物当前不可申请领养");
        }

        Object reviewModeObj = com.petadoption.petadoption.controller.AdminController.getSetting("reviewMode");
        String reviewMode = (reviewModeObj != null) ? String.valueOf(reviewModeObj) : "manual";
        int initialStatus = "auto".equals(reviewMode) ? 1 : 0;

        PetAdoption adoption = new PetAdoption();
        adoption.setPetId(petId);
        adoption.setUserId(applicantId);
        adoption.setReason(message);
        adoption.setStatus(initialStatus);
        adoption.setApplyTime(LocalDateTime.now());
        save(adoption);

        if (initialStatus == 1) {
            pet.setOwnerId(applicantId);
            pet.setStatus(3);
            adoption.setReviewTime(LocalDateTime.now());
            updateById(adoption);
        } else {
            pet.setStatus(2);
        }
        petService.updateById(pet);

        activityLogService.logActivity(applicantId, "adoption", "pet", petId, "提交了领养申请：" + pet.getName());

        return adoption;
    }

    @Override
    @Transactional
    public PetAdoption applyForAdoptionWithDetails(Long petId, Long applicantId, String reason, String contactName, String phone, String address, String housingType, Integer experience, String experienceDesc, String petPlan, String workInfo, String familyInfo, String financialStatus) {
        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getPetId, petId)
               .eq(PetAdoption::getUserId, applicantId)
               .eq(PetAdoption::getStatus, 0);

        if (count(wrapper) > 0) {
            throw new BusinessException(ResponseCode.ADOPTION_STATUS_ERROR);
        }

        Pet pet = petService.getById(petId);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        
        if (pet.getStatus() == null || pet.getStatus() != 1) {
            throw new BusinessException("该宠物当前不可申请领养");
        }

        PetAdoption adoption = new PetAdoption();
        adoption.setPetId(petId);
        adoption.setUserId(applicantId);
        adoption.setReason(reason);
        adoption.setContactName(contactName);
        adoption.setPhone(phone);
        adoption.setAddress(address);
        adoption.setHousingType(housingType);
        adoption.setExperience(experience);
        adoption.setExperienceDesc(experienceDesc);
        adoption.setPetPlan(petPlan);
        adoption.setWorkInfo(workInfo);
        adoption.setFamilyInfo(familyInfo);
        adoption.setFinancialStatus(financialStatus);
        adoption.setStatus(0);
        adoption.setApplyTime(LocalDateTime.now());
        save(adoption);

        pet.setStatus(2);
        petService.updateById(pet);
        
        return adoption;
    }

    @Override
    @Transactional
    public PetAdoption auditAdoption(Long adoptionId, Integer status, String reason, Long auditorId) {
        PetAdoption adoption = getById(adoptionId);
        if (adoption == null) {
            throw new BusinessException(ResponseCode.ADOPTION_NOT_FOUND);
        }

        adoption.setStatus(status);
        adoption.setReviewComment(reason);
        adoption.setReviewTime(LocalDateTime.now());
        adoption.setReviewerId(auditorId);

        updateById(adoption);
        return adoption;
    }

    @Override
    public List<PetAdoption> getUserAdoptions(Long userId) {
        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getUserId, userId)
               .orderByDesc(PetAdoption::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<PetAdoption> getUserAdoptionsByStatus(Long userId, Integer status) {
        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getUserId, userId)
               .eq(PetAdoption::getStatus, status)
               .orderByDesc(PetAdoption::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<PetAdoption> getPetAdoptions(Long petId) {
        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getPetId, petId)
               .orderByDesc(PetAdoption::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<PetAdoption> getPendingAdoptions(int page, int size) {
        Page<PetAdoption> adoptionPage = new Page<>(page, size);
        LambdaQueryWrapper<PetAdoption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAdoption::getStatus, 0)
               .orderByDesc(PetAdoption::getApplyTime);
        return page(adoptionPage, wrapper);
    }

    @Override
    @Transactional
    public PetAdoption reviewAdoption(Long adoptionId, Integer status, String rejectReason, Long auditorId) {
        PetAdoption adoption = getById(adoptionId);
        if (adoption == null) {
            throw new BusinessException(ResponseCode.ADOPTION_NOT_FOUND);
        }

        adoption.setStatus(status);
        if (rejectReason != null) {
            adoption.setRejectReason(rejectReason);
        }
        adoption.setReviewTime(LocalDateTime.now());
        adoption.setReviewerId(auditorId);

        if (status == 1) {
            User applicant = userService.getById(adoption.getUserId());
            if (applicant == null || !"USER".equals(applicant.getRole())) {
                throw new BusinessException("只有普通用户角色才能成为领养人");
            }
            
            Pet pet = petService.getById(adoption.getPetId());
            if (pet != null) {
                pet.setOwnerId(adoption.getUserId());
                pet.setStatus(3);
                petService.updateById(pet);
            }
            
            Integer userAdoptionCount = applicant.getAdoptionCount();
            if (userAdoptionCount == null) userAdoptionCount = 0;
            applicant.setAdoptionCount(userAdoptionCount + 1);
            userService.updateById(applicant);
            
            // 记录活动日志 - 领养通过
            String petName = pet != null ? pet.getName() : "宠物" + adoption.getPetId();
            activityLogService.logActivity(adoption.getUserId(), "adoption_approved", "pet", adoption.getPetId(), "领养申请已通过：" + petName);
        } else if (status == -1) {
            // 记录活动日志 - 领养拒绝
            Pet pet = petService.getById(adoption.getPetId());
            String petName = pet != null ? pet.getName() : "宠物" + adoption.getPetId();
            activityLogService.logActivity(adoption.getUserId(), "adoption_rejected", "pet", adoption.getPetId(), "领养申请被拒绝：" + petName);
        }

        updateById(adoption);
        return adoption;
    }

    @Override
    @Transactional
    public PetAdoption confirmAdoption(Long adoptionId, Long userId) {
        PetAdoption adoption = getById(adoptionId);
        if (adoption == null) {
            throw new BusinessException(ResponseCode.ADOPTION_NOT_FOUND);
        }

        if (!adoption.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.PERMISSION_DENIED);
        }

        if (adoption.getStatus() != 1) {
            throw new BusinessException(ResponseCode.ADOPTION_STATUS_ERROR);
        }

        adoption.setStatus(4);
        adoption.setCompleteTime(LocalDateTime.now());
        updateById(adoption);
        return adoption;
    }

    @Override
    @Transactional
    public PetAdoption rateAdoption(Long adoptionId, Long userId, Integer rating, String comment) {
        PetAdoption adoption = getById(adoptionId);
        if (adoption == null) {
            throw new BusinessException(ResponseCode.ADOPTION_NOT_FOUND);
        }

        if (!adoption.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.PERMISSION_DENIED);
        }

        if (adoption.getStatus() != 4) {
            throw new BusinessException(ResponseCode.ADOPTION_STATUS_ERROR);
        }

        adoption.setRating(rating);
        adoption.setComment(comment);
        adoption.setRateTime(LocalDateTime.now());
        updateById(adoption);
        return adoption;
    }

    @Override
    @Transactional
    public void cancelAdoption(Long adoptionId, Long userId) {
        PetAdoption adoption = getById(adoptionId);
        if (adoption == null) {
            throw new BusinessException(ResponseCode.ADOPTION_NOT_FOUND);
        }

        if (!adoption.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.PERMISSION_DENIED);
        }

        if (adoption.getStatus() != 0) {
            throw new BusinessException(ResponseCode.ADOPTION_STATUS_ERROR);
        }

        adoption.setStatus(3);
        updateById(adoption);

        Pet pet = petService.getById(adoption.getPetId());
        if (pet != null) {
            pet.setStatus(1);
            petService.updateById(pet);
        }
    }
}
