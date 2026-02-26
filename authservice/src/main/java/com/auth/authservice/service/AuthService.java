package com.auth.authservice.service;

import com.auth.authservice.dto.*;
import com.auth.authservice.entity.*;
import com.auth.authservice.repository.*;
import com.auth.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthDto.MessageResponse sendRegisterOtp(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty() && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        // Delete any existing OTP for this email and flush
        otpRepository.findByEmailAndType(request.getEmail(), Otp.OtpType.REGISTER)
                .ifPresent(existingOtp -> {
                    otpRepository.delete(existingOtp);
                    otpRepository.flush();
                });

        String emailOtp = generateOtp();

        Otp otp = Otp.builder()
                .email(request.getEmail())
                .emailOtp(emailOtp)
                .type(Otp.OtpType.REGISTER)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(otp);
        
        try {
            emailService.sendOtpEmail(request.getEmail(), emailOtp);
        } catch (Exception e) {
            log.error("Failed to send email, but OTP saved: {}", e.getMessage());
            // Continue even if email fails - OTP is saved
        }

        log.info("OTP sent for registration: {}", request.getEmail());
        return AuthDto.MessageResponse.builder()
                .message("OTP sent to email")
                .build();
    }

    @Transactional
    public AuthDto.MessageResponse verifyRegisterOtp(AuthDto.VerifyOtpRequest request, RegisterRequest registerData) {
        Otp otp = otpRepository.findByEmailAndType(request.getEmail(), Otp.OtpType.REGISTER)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getEmailOtp().equals(request.getEmailOtp())) {
            throw new RuntimeException("Invalid email OTP");
        }

        User user = User.builder()
                .firstName(registerData.getFirstName())
                .lastName(registerData.getLastName())
                .email(registerData.getEmail())
                .phone(registerData.getPhone())
                .password(passwordEncoder.encode(registerData.getPassword()))
                .emailVerified(true)
                .active(true)
                .build();

        userRepository.save(user);
        otpRepository.delete(otp);

        log.info("User registered successfully: {}", user.getEmail());
        return AuthDto.MessageResponse.builder()
                .message("Registration successful")
                .build();
    }

    @Transactional
    public AuthDto.MessageResponse resendRegisterOtp(AuthDto.ResendOtpRequest request) {
        Otp existingOtp = otpRepository.findByEmailAndType(request.getEmail(), Otp.OtpType.REGISTER)
                .orElseThrow(() -> new RuntimeException("No pending registration found"));

        String emailOtp = generateOtp();

        existingOtp.setEmailOtp(emailOtp);
        existingOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        existingOtp.setCreatedAt(LocalDateTime.now());

        otpRepository.save(existingOtp);
        emailService.sendOtpEmail(request.getEmail(), emailOtp);

        return AuthDto.MessageResponse.builder()
                .message("OTP resent successfully")
                .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> {
                    if (request.getEmailOrPhone() != null && !request.getEmailOrPhone().isEmpty()) {
                        return userRepository.findByPhone(request.getEmailOrPhone());
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            throw new RuntimeException("Account not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .message("Login successful")
                .build();
    }

    @Transactional
    public AuthDto.MessageResponse sendForgotPasswordOtp(AuthDto.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete any existing OTP for this email and flush
        otpRepository.findByEmailAndType(request.getEmail(), Otp.OtpType.FORGOT_PASSWORD)
                .ifPresent(existingOtp -> {
                    otpRepository.delete(existingOtp);
                    otpRepository.flush();
                });

        String emailOtp = generateOtp();

        Otp otp = Otp.builder()
                .email(request.getEmail())
                .emailOtp(emailOtp)
                .type(Otp.OtpType.FORGOT_PASSWORD)
                .expiresAt(LocalDateTime.now().plusMinutes(1))
                .build();

        otpRepository.save(otp);
        
        try {
            emailService.sendOtpEmail(request.getEmail(), emailOtp);
        } catch (Exception e) {
            log.error("Failed to send email, but OTP saved: {}", e.getMessage());
            // Continue even if email fails - OTP is saved
        }

        return AuthDto.MessageResponse.builder()
                .message("OTP sent to email")
                .build();
    }

    @Transactional
    public AuthDto.AuthResponse verifyForgotOtp(AuthDto.VerifyForgotOtpRequest request) {
        Otp otp = otpRepository.findByEmailAndType(request.getEmail(), Otp.OtpType.FORGOT_PASSWORD)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getEmailOtp().equals(request.getEmailOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        String resetToken = jwtUtil.generateToken(request.getEmail());
        otpRepository.delete(otp);

        return AuthDto.AuthResponse.builder()
                .token(resetToken)
                .message("OTP verified")
                .build();
    }

    @Transactional
    public AuthDto.MessageResponse resetPassword(AuthDto.ResetPasswordRequest request) {
        if (!jwtUtil.validateToken(request.getToken())) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return AuthDto.MessageResponse.builder()
                .message("Password reset successful")
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
