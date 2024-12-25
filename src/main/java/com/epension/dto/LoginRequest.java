package com.epension.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phoneNumber;
    private String password;
    private String role;

    @Override
    public String toString() {
        return "LoginRequest{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                ", passwordLength=" + (password != null ? password.length() : 0) +
                '}';
    }
}
