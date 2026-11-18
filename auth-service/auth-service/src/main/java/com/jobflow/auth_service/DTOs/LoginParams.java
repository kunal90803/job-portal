package com.jobflow.auth_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginParams {
    @NotBlank(message = "Can't be null")
    private String email;

    @NotBlank(message = "Can't be null")
    private String password;

}
