package com.petadoption.petadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadoption.petadoption.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    Optional<User> selectByUsername(@Param("username") String username);
    boolean existsByUsername(@Param("username") String username);
    boolean existsByEmail(@Param("email") String email);
    boolean existsByPhone(@Param("phone") String phone);
}
