package com.ihsan.aidatflow.service;

import com.ihsan.aidatflow.config.JwtService;
import com.ihsan.aidatflow.dto.AuthResponse;
import com.ihsan.aidatflow.dto.LoginRequest;
import com.ihsan.aidatflow.dto.RegisterRequest;
import com.ihsan.aidatflow.entity.AppUser;
import com.ihsan.aidatflow.entity.UserRole;
import com.ihsan.aidatflow.repository.AppUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ADMIN);

        AppUser saved = appUserRepository.save(user);
        String token = jwtService.generate(saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, "Bearer", saved.getEmail(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generate(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getRole().name());
    }
}
