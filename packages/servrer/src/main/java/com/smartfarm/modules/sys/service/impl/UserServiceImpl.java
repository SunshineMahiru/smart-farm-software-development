package com.smartfarm.modules.sys.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.sys.dto.LoginRequest;
import com.smartfarm.modules.sys.entity.User;
import com.smartfarm.modules.sys.mapper.UserMapper;
import com.smartfarm.modules.sys.service.UserService;
import com.smartfarm.modules.sys.vo.LoginVO;
import com.smartfarm.modules.sys.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        StpUtil.login(user.getUserId());
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "Current user does not exist");
        }

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public String getRoleByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? null : user.getRole();
    }
}
