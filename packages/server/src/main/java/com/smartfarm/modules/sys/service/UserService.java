package com.smartfarm.modules.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.sys.dto.UserCreateRequest;
import com.smartfarm.modules.sys.dto.UserUpdateRequest;
import com.smartfarm.modules.sys.dto.LoginRequest;
import com.smartfarm.modules.sys.vo.LoginVO;
import com.smartfarm.modules.sys.vo.UserInfoVO;

public interface UserService {

    LoginVO login(LoginRequest request);

    UserInfoVO getCurrentUser();

    String getRoleByUserId(Long userId);

    IPage<UserInfoVO> pageUsers(long pageNum, long pageSize, String keyword, String role);

    UserInfoVO getUserById(Long userId);

    void createUser(UserCreateRequest request);

    void updateUser(Long userId, UserUpdateRequest request);

    void resetPassword(Long userId, String password);

    void deleteUser(Long userId);
}
