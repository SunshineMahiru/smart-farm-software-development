package com.smartfarm.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.smartfarm.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 拦截 Sa-Token 未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handlerNotLoginException(NotLoginException e) {
        log.warn("用户未登录或Token已过期: {}", e.getMessage());
        return Result.error(401, "请先登录系统");
    }

    // 拦截越权访问异常
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handlerNotRoleException(NotRoleException e) {
        log.warn("越权访问: {}", e.getMessage());
        return Result.error(403, "当前角色无权限进行此操作");
    }

    // 兜底拦截其他所有未知异常
    @ExceptionHandler(Exception.class)
    public Result<?> handlerException(Exception e) {
        log.error("系统内部异常: ", e);
        return Result.error(500, "系统繁忙，请稍后再试：" + e.getMessage());
    }
}