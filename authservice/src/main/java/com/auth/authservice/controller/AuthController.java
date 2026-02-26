package com.auth.authservice.controller;

import com.auth.authservice.dto.*;
import com.auth.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Map<String, RegisterRequest> pendingRegistrations = new ConcurrentHashMap<>();

    @PostMapping("/register/send-otp")
    public ResponseEntity<AuthDto.MessageResponse> sendRegisterOtp(@Valid @RequestBody RegisterRequest request) {
        AuthDto.MessageResponse response = authService.sendRegisterOtp(request);
        pendingRegistrations.put(request.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/verify")
    public ResponseEntity<AuthDto.MessageResponse> verifyRegisterOtp(@Valid @RequestBody AuthDto.VerifyOtpRequest request) {
        RegisterRequest registerData = pendingRegistrations.get(request.getEmail());
        if (registerData == null) {
            return ResponseEntity.badRequest().body(
                AuthDto.MessageResponse.builder().message("Registration data not found").build()
            );
        }
        
        AuthDto.MessageResponse response = authService.verifyRegisterOtp(request, registerData);
        pendingRegistrations.remove(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<AuthDto.MessageResponse> resendRegisterOtp(@Valid @RequestBody AuthDto.ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendRegisterOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot/send-otp")
    public ResponseEntity<AuthDto.MessageResponse> sendForgotPasswordOtp(@Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.sendForgotPasswordOtp(request));
    }

    @PostMapping("/forgot/resend-otp")
    public ResponseEntity<AuthDto.MessageResponse> resendForgotPasswordOtp(@Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.sendForgotPasswordOtp(request));
    }

    @PostMapping("/forgot/verify")
    public ResponseEntity<AuthDto.AuthResponse> verifyForgotOtp(@Valid @RequestBody AuthDto.VerifyForgotOtpRequest request) {
        return ResponseEntity.ok(authService.verifyForgotOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthDto.MessageResponse> resetPassword(@Valid @RequestBody AuthDto.ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthDto.MessageResponse> handleException(RuntimeException e) {
        return ResponseEntity.badRequest().body(
            AuthDto.MessageResponse.builder().message(e.getMessage()).build()
        );
    }
}
