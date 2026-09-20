package com.bankwise.auth.service;

import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    void logout(String authHeader);
}