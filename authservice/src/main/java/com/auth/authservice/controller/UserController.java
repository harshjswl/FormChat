package com.auth.authservice.controller;

import com.auth.authservice.dto.AuthDto;
import com.auth.authservice.dto.UserDto;
import com.auth.authservice.security.JwtUtil;
import com.auth.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<UserDto.ProfileResponse> getProfile(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(userService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto.ProfileResponse> updateProfile(
            @Valid @RequestBody UserDto.UpdateProfileRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(userService.updateProfile(request, email));
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthDto.MessageResponse> changePassword(
            @Valid @RequestBody UserDto.ChangePasswordRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(userService.changePassword(request, email));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthDto.MessageResponse> handleException(RuntimeException e) {
        return ResponseEntity.badRequest().body(
            AuthDto.MessageResponse.builder().message(e.getMessage()).build()
        );
    }
}
