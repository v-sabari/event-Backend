package com.example.Backend.controller;

import com.example.Backend.dto.LoginDTO;
import com.example.Backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody LoginDTO dto){
        return userService.login(dto);
    }
}