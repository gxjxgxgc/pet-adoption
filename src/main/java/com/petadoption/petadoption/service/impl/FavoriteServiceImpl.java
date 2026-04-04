package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetFavorite;
import com.petadoption.petadoption.entity.User;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.PetFavoriteMapper;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.service.ActivityLogService;
import com.petadoption.petadoption.service.FavoriteService;
import com.petadoption.petadoption.service.PetService;
import com.petadoption.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<PetFavoriteMapper, PetFavorite> implements FavoriteService {

    private final PetFavoriteMapper petFavoriteMapper;
    private final PetService petService;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public void favorite(Long userId, Long petId) {
        // 检查是否已收藏
        PetFavorite existing = petFavoriteMapper.selectByUserIdAndPetId(userId, petId);
        if (existing != null) {
            throw new BusinessException(ResponseCode.FAVORITE_ALREADY_EXISTS);
        }

        PetFavorite favorite = new PetFavorite();
        favorite.setUserId(userId);
        favorite.setPetId(petId);
        save(favorite);

        // 更新宠物的收藏数
        Pet pet = petService.getById(petId);
        if (pet != null) {
            Integer count = pet.getFavoriteCount();
            if (count == null) count = 0;
            pet.setFavoriteCount(count + 1);
            petService.updateById(pet);
        }

        // 更新用户的收藏数
        User user = userService.getById(userId);
        if (user != null) {
            Integer userFavCount = user.getFavoriteCount();
            if (userFavCount == null) userFavCount = 0;
            user.setFavoriteCount(userFavCount + 1);
            userService.updateById(user);
        }

        // 记录活动日志
        String petName = pet != null ? pet.getName() : "宠物" + petId;
        activityLogService.logActivity(userId, "favorite", "pet", petId, "收藏了宠物：" + petName);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long petId) {
        PetFavorite favorite = petFavoriteMapper.selectByUserIdAndPetId(userId, petId);
        if (favorite == null) {
            throw new BusinessException(ResponseCode.FAVORITE_NOT_FOUND);
        }
        removeById(favorite.getId());

        // 更新宠物的收藏数
        Pet pet = petService.getById(petId);
        if (pet != null) {
            Integer count = pet.getFavoriteCount();
            if (count == null || count <= 0) count = 0;
            else count = count - 1;
            pet.setFavoriteCount(count);
            petService.updateById(pet);
        }

        // 更新用户的收藏数
        User user = userService.getById(userId);
        if (user != null) {
            Integer userFavCount = user.getFavoriteCount();
            if (userFavCount == null || userFavCount <= 0) userFavCount = 0;
            else userFavCount = userFavCount - 1;
            user.setFavoriteCount(userFavCount);
            userService.updateById(user);
        }

        // 记录活动日志
        String petName = pet != null ? pet.getName() : "宠物" + petId;
        activityLogService.logActivity(userId, "unfavorite", "pet", petId, "取消收藏了宠物：" + petName);
    }

    @Override
    @Transactional
    public void batchUnfavorite(Long userId, List<Long> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<PetFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetFavorite::getUserId, userId)
               .in(PetFavorite::getPetId, petIds);
        remove(wrapper);

        // 更新每个被取消收藏的宠物的收藏数
        for (Long petId : petIds) {
            Pet pet = petService.getById(petId);
            if (pet != null) {
                Integer count = pet.getFavoriteCount();
                if (count == null || count <= 0) count = 0;
                else count = count - 1;
                pet.setFavoriteCount(count);
                petService.updateById(pet);
            }
        }

        // 更新用户的收藏数
        int removedCount = petIds.size();
        User user = userService.getById(userId);
        if (user != null) {
            Integer userFavCount = user.getFavoriteCount();
            if (userFavCount == null || userFavCount <= 0) userFavCount = 0;
            else userFavCount = userFavCount - removedCount;
            if (userFavCount < 0) userFavCount = 0;
            user.setFavoriteCount(userFavCount);
            userService.updateById(user);
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long petId) {
        PetFavorite favorite = petFavoriteMapper.selectByUserIdAndPetId(userId, petId);
        return favorite != null;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long petId) {
        if (isFavorited(userId, petId)) {
            unfavorite(userId, petId);
            return false;
        } else {
            favorite(userId, petId);
            return true;
        }
    }

    @Override
    public List<PetFavorite> getUserFavorites(Long userId, String keyword, String sortBy, String sortOrder) {
        LambdaQueryWrapper<PetFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetFavorite::getUserId, userId);
        
        List<PetFavorite> allFavorites = list(wrapper);
        
        if (allFavorites.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<Long> petIds = allFavorites.stream()
            .map(PetFavorite::getPetId)
            .collect(java.util.stream.Collectors.toList());
        
        LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
        petWrapper.in(Pet::getId, petIds);
        
        if (keyword != null && !keyword.isEmpty()) {
            petWrapper.and(w -> w.like(Pet::getName, keyword)
                .or().like(Pet::getBreed, keyword));
        }
        
        List<Pet> pets = petService.list(petWrapper);
        
        if (pets.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<Long> matchedPetIds = pets.stream()
            .map(Pet::getId)
            .collect(java.util.stream.Collectors.toList());
        
        List<PetFavorite> filteredFavorites = allFavorites.stream()
            .filter(f -> matchedPetIds.contains(f.getPetId()))
            .collect(java.util.stream.Collectors.toList());
        
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createTime";
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        
        filteredFavorites.sort((f1, f2) -> {
            Pet p1 = pets.stream().filter(p -> p.getId().equals(f1.getPetId())).findFirst().orElse(null);
            Pet p2 = pets.stream().filter(p -> p.getId().equals(f2.getPetId())).findFirst().orElse(null);
            
            int result = 0;
            switch (sortField) {
                case "name":
                    String name1 = p1 != null ? p1.getName() : "";
                    String name2 = p2 != null ? p2.getName() : "";
                    result = name1.compareTo(name2);
                    break;
                case "createTime":
                default:
                    result = f1.getCreateTime().compareTo(f2.getCreateTime());
                    break;
            }
            return ascending ? result : -result;
        });
        
        return filteredFavorites;
    }
}
