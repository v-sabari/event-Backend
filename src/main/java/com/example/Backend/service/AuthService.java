package com.example.Backend.service;

import com.example.Backend.dto.LoginDTO;
import com.example.Backend.dto.auth.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginDTO dto, String clientIp);

    AuthResponseDTO refresh(String refreshToken);

    void logout(String refreshToken);
}