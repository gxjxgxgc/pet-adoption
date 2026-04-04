package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadoption.petadoption.entity.PetComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetCommentMapper extends BaseMapper<PetComment> {
}
