package com.example.Backend.service;

import com.example.Backend.repository.UserRepository;
import com.example.Backend.dto.LoginDTO;
import com.example.Backend.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {

        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByRegNumber(dto.getRegNumber());

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            if (user.getPassword().equals(dto.getPassword())) {

                response.put("status", true);
                response.put("role", user.getRole());
                response.put("name", user.getName());

                return response;
            }
        }

        response.put("status", false);
        response.put("message", "Invalid Credentials");

        return response;
    }
}