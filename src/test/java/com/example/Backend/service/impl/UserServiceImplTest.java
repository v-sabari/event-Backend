package com.example.Backend.service.impl;

import com.example.Backend.dto.auth.RegisterRequestDTO;
import com.example.Backend.exception.DuplicateResourceException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Department;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.DepartmentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for UserServiceImpl driven by Mockito - no Spring context
 * and no live database, so `mvn test` runs anywhere.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDTO validDto(String regNumber, String email, String role, Long departmentId) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setRegNumber(regNumber);
        dto.setName("Jane Doe");
        dto.setEmail(email);
        dto.setPassword("password123");
        dto.setRole(role);
        dto.setDepartmentId(departmentId);
        return dto;
    }

    @Test
    void registerRejectsDuplicateRegNumber() {
        RegisterRequestDTO dto = validDto("2023CS001", "jane@example.com", "STUDENT", null);
        when(userRepository.existsByRegNumber("2023CS001")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
        verify(auditLogService, never()).record(anyString(), anyString(), any(), anyString());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequestDTO dto = validDto("2023CS002", "dup@example.com", "STUDENT", null);
        when(userRepository.existsByRegNumber("2023CS002")).thenReturn(false);
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsUnknownRole() {
        RegisterRequestDTO dto = validDto("2023CS003", "bad@example.com", "NINJA", null);
        when(userRepository.existsByRegNumber("2023CS003")).thenReturn(false);
        when(userRepository.existsByEmail("bad@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAcceptsRoleIgnoringCaseAndWhitespace() {
        RegisterRequestDTO dto = validDto("2023CS004", "case@example.com", " student ", 10L);
        when(userRepository.existsByRegNumber("2023CS004")).thenReturn(false);
        when(userRepository.existsByEmail("case@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department(10L)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        User result = userService.register(dto);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.getPassword()).isEqualTo("$2a$encoded");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getDepartment()).isNotNull();

        verify(passwordEncoder).encode("password123");
        verify(auditLogService).record(eq("USER_REGISTERED"), eq("User"), eq(99L),
                anyString());
    }

    @Test
    void registerWithoutDepartmentLeavesItNull() {
        RegisterRequestDTO dto = validDto("2023CS005", "nodept@example.com", "STUDENT", null);
        when(userRepository.existsByRegNumber("2023CS005")).thenReturn(false);
        when(userRepository.existsByEmail("nodept@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        User result = userService.register(dto);

        assertThat(result.getDepartment()).isNull();
        verify(departmentRepository, never()).findById(anyLong());
    }

    @Test
    void registerThrowsWhenDepartmentMissing() {
        RegisterRequestDTO dto = validDto("2023CS006", "dept@example.com", "STUDENT", 77L);
        when(userRepository.existsByRegNumber("2023CS006")).thenReturn(false);
        when(userRepository.existsByEmail("dept@example.com")).thenReturn(false);
        when(departmentRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByRegNumberReturnsUserWhenPresent() {
        User existing = user(11L, "2022ME010");
        when(userRepository.findByRegNumber("2022ME010")).thenReturn(Optional.of(existing));

        assertThat(userService.findByRegNumber("2022ME010")).isEqualTo(existing);
    }

    @Test
    void findByRegNumberThrowsWhenMissing() {
        when(userRepository.findByRegNumber("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByRegNumber("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void setEnabledTogglesAndAudits() {
        User existing = user(12L, "2022EC045");
        existing.setEnabled(true);
        when(userRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.setEnabled(12L, false);

        assertThat(result.isEnabled()).isFalse();
        verify(auditLogService).record(eq("USER_DISABLED"), eq("User"), eq(12L), anyString());
    }

    @Test
    void changeRoleUpdatesAndAudits() {
        User existing = user(13L, "2022IT077");
        existing.setRole(Role.STUDENT);
        when(userRepository.findById(13L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.changeRole(13L, Role.HOD);

        assertThat(result.getRole()).isEqualTo(Role.HOD);
        verify(auditLogService).record(eq("USER_ROLE_CHANGED"), eq("User"), eq(13L), anyString());
    }

    private Department department(Long id) {
        Department department = new Department();
        department.setId(id);
        department.setName("Computer Science");
        department.setCode("CS");
        return department;
    }

    private User user(Long id, String regNumber) {
        User user = new User();
        user.setId(id);
        user.setRegNumber(regNumber);
        user.setName("Test User");
        user.setEmail(regNumber + "@example.com");
        user.setPassword("encoded");
        user.setRole(Role.STUDENT);
        return user;
    }
}