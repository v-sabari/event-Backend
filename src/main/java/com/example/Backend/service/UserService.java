package com.example.Backend.service;

import com.example.Backend.dto.auth.RegisterRequestDTO;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;

import java.util.List;

/**
 * Owns user account data/lifecycle only (SRP). Authentication concerns
 * (login, tokens, OTP) live in AuthService/PasswordResetService instead of
 * being mixed into this class as they were before.
 */
public interface UserService {

    User register(RegisterRequestDTO dto);

    User findByRegNumber(String regNumber);

    User findByEmail(String email);

    User findById(Long id);

    List<User> findAll();

    List<User> findByRole(Role role);

    User setEnabled(Long id, boolean enabled);

    User changeRole(Long id, Role role);
}
