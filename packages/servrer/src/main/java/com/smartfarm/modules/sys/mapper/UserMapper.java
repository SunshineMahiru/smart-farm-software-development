package com.smartfarm.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfarm.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
