package com.auth.authservice.repository;

import com.auth.authservice.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndType(String email, Otp.OtpType type);
}
