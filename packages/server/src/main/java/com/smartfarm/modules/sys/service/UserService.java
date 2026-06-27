package com.smartfarm.modules.sys.service;

import com.smartfarm.modules.sys.dto.LoginRequest;
import com.smartfarm.modules.sys.vo.LoginVO;
import com.smartfarm.modules.sys.vo.UserInfoVO;

public interface UserService {

    LoginVO login(LoginRequest request);

    UserInfoVO getCurrentUser();

    String getRoleByUserId(Long userId);
}
