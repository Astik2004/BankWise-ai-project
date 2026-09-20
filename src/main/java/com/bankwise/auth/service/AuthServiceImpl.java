package com.bankwise.auth.service;

import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse register(RegisterRequest request) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {

    }

    @Override
    public void logout(String authHeader) {

    }
}
