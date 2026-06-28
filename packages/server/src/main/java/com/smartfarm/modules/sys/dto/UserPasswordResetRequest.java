package com.smartfarm.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户密码重置请求")
public class UserPasswordResetRequest {

    @Schema(description = "新密码", example = "123456")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String password;
}
