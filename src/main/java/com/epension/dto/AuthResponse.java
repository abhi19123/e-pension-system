package com.epension.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private boolean success;
    private String token;
    private UserDto user;
    private String message;
}
