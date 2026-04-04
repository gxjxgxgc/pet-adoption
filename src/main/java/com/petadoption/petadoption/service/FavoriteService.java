package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.PetFavorite;

public interface FavoriteService extends IService<PetFavorite> {

    /**
     * 收藏宠物
     */
    void favorite(Long userId, Long petId);

    /**
     * 取消收藏
     */
    void unfavorite(Long userId, Long petId);

    /**
     * 批量取消收藏
     */
    void batchUnfavorite(Long userId, java.util.List<Long> petIds);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, Long petId);

    /**
     * 切换收藏状态
     */
    boolean toggleFavorite(Long userId, Long petId);

    /**
     * 获取用户的收藏列表
     */
    java.util.List<PetFavorite> getUserFavorites(Long userId, String keyword, String sortBy, String sortOrder);
}
