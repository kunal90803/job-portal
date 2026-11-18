package com.jobflow.auth_service.DTOs;

import com.jobflow.auth_service.Enums.UserRoles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthParams {
    @NotBlank(message = "Name can't be null")
    private String name;

    @NotBlank(message = "Password can't be null")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @NotNull(message = "Roles can't be null")
    private UserRoles roles;

    @NotBlank(message = "Password can't be null")
    private String email;

}
