package com.smartfarm.modules.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartfarm.common.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sys/user")
public class UserController {

    // 这是一个白名单接口（我们在 SaTokenConfigure 里放行了它）
    @PostMapping("/login")
    public Result<Map<String, Object>> login(String username, String password) {
        // 这里为了打通链路，写死校验。后续你再换成查数据库。
        if ("admin".equals(username) && "123456".equals(password)) {
            // Sa-Token 登录，传入用户ID (假设为 1)
            StpUtil.login(1);

            // 获取 Token
            String tokenValue = StpUtil.getTokenValue();
            Map<String, Object> map = new HashMap<>();
            map.put("token", tokenValue);
            map.put("userId", 1);

            return Result.success(map);
        }
        return Result.error(400, "用户名或密码错误");
    }

    // 这是一个需要鉴权的接口，如果不带 Token 访问，会被 GlobalExceptionHandler 拦截
    @GetMapping("/info")
    public Result<String> getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success("成功获取用户信息，当前登录用户ID：" + userId);
    }
}