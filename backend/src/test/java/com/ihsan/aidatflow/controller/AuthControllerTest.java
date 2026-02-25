package com.ihsan.aidatflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsan.aidatflow.dto.AuthResponse;
import com.ihsan.aidatflow.dto.LoginRequest;
import com.ihsan.aidatflow.dto.RegisterRequest;
import com.ihsan.aidatflow.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ihsan.aidatflow.config.JwtAuthenticationFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_returnsToken() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse("tkn", "Bearer", "ihsan@example.com", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("Ihsan", "ihsan@example.com", "123456"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("tkn"));
    }

    @Test
    void login_returnsToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("tkn2", "Bearer", "ihsan@example.com", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ihsan@example.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tkn2"));
    }

    @Test
    void register_existingEmail_returnsConflict() throws Exception {
        doThrow(new IllegalArgumentException("Email already exists")).when(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("Ihsan", "ihsan@example.com", "123456"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
}
