package com.smartfarm.common.security;

import cn.dev33.satoken.stp.StpInterface;
import com.smartfarm.modules.sys.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String role = userService.getRoleByUserId(Long.parseLong(String.valueOf(loginId)));
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }

        List<String> permissions = new ArrayList<>();
        permissions.add("supplier:read");
        permissions.add("sensor:read");
        permissions.add("farm:read");
        permissions.add("plot:read");
        permissions.add("user:read");
        if ("管理员".equals(role)) {
            permissions.add("supplier:write");
            permissions.add("sensor:write");
            permissions.add("farm:write");
            permissions.add("plot:write");
            permissions.add("user:manage");
        }
        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String role = userService.getRoleByUserId(Long.parseLong(String.valueOf(loginId)));
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role);
    }
}
