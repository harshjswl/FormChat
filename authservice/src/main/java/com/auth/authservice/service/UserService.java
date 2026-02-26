package com.auth.authservice.service;

import com.auth.authservice.dto.AuthDto;
import com.auth.authservice.dto.UserDto;
import com.auth.authservice.entity.User;
import com.auth.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToProfileResponse(user);
    }

    @Transactional
    public UserDto.ProfileResponse updateProfile(UserDto.UpdateProfileRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return mapToProfileResponse(user);
    }

    @Transactional
    public AuthDto.MessageResponse changePassword(UserDto.ChangePasswordRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return AuthDto.MessageResponse.builder()
                .message("Password changed successfully")
                .build();
    }

    private UserDto.ProfileResponse mapToProfileResponse(User user) {
        return UserDto.ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .reputation(user.getReputation())
                .createdAt(user.getCreatedAt())
                .lastSeen(user.getLastSeen())
                .build();
    }
}
