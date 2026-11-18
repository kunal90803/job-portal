package com.jobflow.auth_service.Responses;

import com.jobflow.auth_service.DTOs.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private UserInfo user;
}