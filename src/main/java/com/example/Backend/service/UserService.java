package com.example.Backend.service;

import com.example.Backend.dto.auth.RegisterRequestDTO;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // BE-17: findAll()/findByRole(Role) returned every matching user
    // unbounded - at real college scale (thousands of students) this and
    // the table rendering it would grow slow and unwieldy. Replaced with
    // the same Page<T> pattern already used correctly in
    // NotificationService/AuditLogService. Neither method had any caller
    // besides UserController, so this is a clean signature swap.
    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    User setEnabled(Long id, boolean enabled);

    User changeRole(Long id, Role role);
}