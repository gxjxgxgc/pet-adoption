package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadoption.petadoption.entity.PetFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetFavoriteMapper extends BaseMapper<PetFavorite> {

    /**
     * 检查用户是否已收藏该宠物
     */
    PetFavorite selectByUserIdAndPetId(@Param("userId") Long userId, @Param("petId") Long petId);

    /**
     * 统计宠物的收藏数量
     */
    int countByPetId(@Param("petId") Long petId);
}
