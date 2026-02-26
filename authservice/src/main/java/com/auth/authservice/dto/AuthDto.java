package com.auth.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyOtpRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Email OTP is required")
        private String emailOtp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyForgotOtpRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Email OTP is required")
        private String emailOtp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResendOtpRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email or phone is required")
        private String emailOrPhone;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "Password is required")
        @Size(min = 6)
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }
}
