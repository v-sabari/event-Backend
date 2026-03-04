package com.example.Backend.service;

import com.example.Backend.dto.LoginDTO;

import java.util.Map;

public interface UserService {

    Map<String, Object> login(LoginDTO dto);

}