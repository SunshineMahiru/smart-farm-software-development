package com.smartfarm.modules.sys.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private String phone;

    private LocalDateTime createdAt;
}
