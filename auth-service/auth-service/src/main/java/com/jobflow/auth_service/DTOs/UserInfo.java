package com.jobflow.auth_service.DTOs;

import com.jobflow.auth_service.Enums.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserInfo {

    private String id;
    private String name;
    private String email;
    private Set<UserRoles> roles;
}