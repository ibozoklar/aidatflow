package com.ihsan.aidatflow.service;

import com.ihsan.aidatflow.config.JwtService;
import com.ihsan.aidatflow.dto.AuthResponse;
import com.ihsan.aidatflow.dto.LoginRequest;
import com.ihsan.aidatflow.dto.RegisterRequest;
import com.ihsan.aidatflow.entity.AppUser;
import com.ihsan.aidatflow.entity.UserRole;
import com.ihsan.aidatflow.repository.AppUserRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AppUserRepository appUserRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    @Test
    void register_createsUserAndToken() {
        RegisterRequest request = new RegisterRequest("Ihsan", "ihsan@example.com", "123456");
        when(appUserRepository.existsByEmail("ihsan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");

        AppUser saved = new AppUser();
        saved.setEmail("ihsan@example.com");
        saved.setRole(UserRole.ADMIN);
        when(appUserRepository.save(any())).thenReturn(saved);
        when(jwtService.generate("ihsan@example.com", "ADMIN")).thenReturn("token");

        AuthResponse response = authService.register(request);
        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void login_failsForWrongPassword() {
        AppUser user = new AppUser();
        user.setEmail("ihsan@example.com");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.ADMIN);
        when(appUserRepository.findByEmail("ihsan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ihsan@example.com", "wrong"))).isInstanceOf(RuntimeException.class);
    }
}
