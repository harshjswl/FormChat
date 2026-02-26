package com.auth.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String emailOtp;
    
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    private OtpType type;

    public enum OtpType {
        REGISTER, FORGOT_PASSWORD
    }
}
