package com.example.Backend.repository;

import com.example.Backend.model.PasswordResetOtp;
import com.example.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    List<PasswordResetOtp> findByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}
