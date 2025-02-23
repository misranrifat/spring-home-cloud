package com.example.springhomecloud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}