package com.example.Backend.security;

import com.example.Backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security to the existing UserRepository. Because User
 * already implements UserDetails, no separate mapping/DTO is needed here.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String regNumber) throws UsernameNotFoundException {
        return userRepository.findByRegNumber(regNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with reg number: " + regNumber));
    }
}
