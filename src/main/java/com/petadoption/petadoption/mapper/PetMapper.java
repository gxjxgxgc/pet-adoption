package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.petadoption.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
    IPage<Pet> selectPetsByStatus(Page<Pet> page, @Param("status") Integer status);
    IPage<Pet> selectPetsByTypeAndStatus(Page<Pet> page, @Param("type") String type, @Param("status") Integer status);
    IPage<Pet> searchPets(Page<Pet> page, @Param("keyword") String keyword);
    IPage<Pet> selectHotPets(Page<Pet> page);
    IPage<Pet> selectPetsByFilter(Page<Pet> page, @Param("keyword") String keyword, @Param("type") String type, @Param("breed") String breed, @Param("gender") Integer gender, @Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge, @Param("minWeight") Double minWeight, @Param("maxWeight") Double maxWeight, @Param("healthStatusList") List<String> healthStatusList, @Param("sortBy") String sortBy, @Param("sortOrder") String sortOrder, @Param("status") Integer status, @Param("statusList") List<Integer> statusList);
    Pet selectPetWithHealthStatuses(@Param("id") Long id);
}
