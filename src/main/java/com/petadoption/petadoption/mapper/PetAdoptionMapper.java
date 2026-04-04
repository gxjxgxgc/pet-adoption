package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.petadoption.entity.PetAdoption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PetAdoptionMapper extends BaseMapper<PetAdoption> {
    IPage<PetAdoption> selectByApplicantId(Page<PetAdoption> page, @Param("applicantId") Long applicantId);
    IPage<PetAdoption> selectByStatus(Page<PetAdoption> page, @Param("status") Integer status);
    List<PetAdoption> selectByPetId(@Param("petId") Long petId);
}
