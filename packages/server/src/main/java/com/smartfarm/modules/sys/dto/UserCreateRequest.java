package com.smartfarm.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @Schema(description = "登录账号，至少4位", example = "tech20")
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 50, message = "Username length must be between 4 and 50")
    private String username;

    @Schema(description = "登录密码", example = "123456")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String password;

    @Schema(description = "真实姓名", example = "赵农技")
    @Size(max = 50, message = "Real name length cannot exceed 50")
    private String realName;

    @Schema(description = "角色，只允许管理员或农技员", example = "农技员")
    @NotBlank(message = "Role cannot be empty")
    @Pattern(regexp = "管理员|农技员", message = "Role must be 管理员 or 农技员")
    private String role;

    @Schema(description = "手机号，可为空", example = "13800000020")
    @Pattern(regexp = "^$|^1\\d{10}$", message = "Phone format is invalid")
    private String phone;
}
