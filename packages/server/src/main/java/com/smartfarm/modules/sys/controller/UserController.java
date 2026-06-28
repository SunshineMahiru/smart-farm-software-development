package com.smartfarm.modules.sys.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.sys.dto.LoginRequest;
import com.smartfarm.modules.sys.dto.UserCreateRequest;
import com.smartfarm.modules.sys.dto.UserPasswordResetRequest;
import com.smartfarm.modules.sys.dto.UserUpdateRequest;
import com.smartfarm.modules.sys.service.UserService;
import com.smartfarm.modules.sys.vo.LoginVO;
import com.smartfarm.modules.sys.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "成员1-用户与权限管理接口")
@RequestMapping("/sys/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户登录", description = "账号密码登录并返回 Sa-Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "退出登录", description = "清理当前登录态")
    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("Logout successful");
    }

    @Operation(summary = "当前用户信息", description = "返回当前登录用户的基础信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @SaCheckPermission("user:read")
    @Operation(summary = "分页查询用户", description = "支持按关键字和角色筛选用户列表，不返回密码字段")
    @GetMapping
    public Result<IPage<UserInfoVO>> pageUsers(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "关键字，匹配账号、姓名或电话", example = "tech")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "角色，管理员或农技员", example = "农技员")
            @RequestParam(required = false) String role) {
        return Result.success(userService.pageUsers(pageNum, pageSize, keyword, role));
    }

    @SaCheckPermission("user:read")
    @Operation(summary = "查询用户详情", description = "根据用户ID查询基础信息，不返回密码字段")
    @GetMapping("/{userId}")
    public Result<UserInfoVO> getUser(@Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @SaCheckPermission("user:manage")
    @Operation(summary = "新增用户", description = "创建系统用户，账号和手机号需唯一")
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return Result.success("User created successfully");
    }

    @SaCheckPermission("user:manage")
    @Operation(summary = "修改用户", description = "更新用户基础资料和角色")
    @PutMapping("/{userId}")
    public Result<String> updateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(userId, request);
        return Result.success("User updated successfully");
    }

    @SaCheckPermission("user:manage")
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户密码")
    @PatchMapping("/{userId}/password")
    public Result<String> resetPassword(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UserPasswordResetRequest request) {
        userService.resetPassword(userId, request.getPassword());
        return Result.success("User password reset successfully");
    }

    @SaCheckPermission("user:manage")
    @Operation(summary = "删除用户", description = "删除未被业务记录引用且非当前登录用户的账号")
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("User deleted successfully");
    }
}
