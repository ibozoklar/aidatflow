package com.ihsan.aidatflow.dto;

public record AuthResponse(String accessToken, String tokenType, String email, String role) {}
