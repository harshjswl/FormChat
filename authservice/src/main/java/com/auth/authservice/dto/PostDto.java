package com.auth.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePostRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be less than 255 characters")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private String category;
        private List<String> tags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePostRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private String category;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostResponse {
        private Long id;
        private String title;
        private String content;
        private String category;
        private List<String> tags;
        private Integer views;
        private Integer likes;
        private Long replyCount;
        private UserInfo user;
        private Boolean isLiked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String avatarUrl;
        private Integer reputation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReplyRequest {
        @NotBlank(message = "Content is required")
        private String content;

        private Long parentReplyId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyResponse {
        private Long id;
        private String content;
        private Integer likes;
        private UserInfo user;
        private Boolean isLiked;
        private Long parentReplyId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
