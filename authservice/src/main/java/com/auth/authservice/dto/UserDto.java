package com.auth.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Size(max = 500, message = "Bio must be less than 500 characters")
        private String bio;

        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String bio;
        private String avatarUrl;
        private Integer reputation;
        private LocalDateTime createdAt;
        private LocalDateTime lastSeen;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
    }
}
