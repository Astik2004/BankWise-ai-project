package com.bankwise.unit.auth.controller;

import com.bankwise.auth.controller.AuthController;
import com.bankwise.auth.dto.AuthResponse;
import com.bankwise.auth.dto.ChangePasswordRequest;
import com.bankwise.auth.dto.LoginRequest;
import com.bankwise.auth.dto.RegisterRequest;
import com.bankwise.auth.service.AuthService;
import com.bankwise.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegisterUser() {

        RegisterRequest request = new RegisterRequest(
                "Astik",
                "Yadav",
                "astik@gmail.com",
                "astik@123"
        );

        AuthResponse authResponse = new AuthResponse(
                null,
                "Bearer"
        );

        when(authService.register(request))
                .thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                authController.register(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully",
                response.getBody().getMessage());
        assertEquals(authResponse,
                response.getBody().getData());

        verify(authService).register(request);
    }

    @Test
    void testLoginUser() {

        LoginRequest request = new LoginRequest(
                "astik@gmail.com",
                "astik@123"
        );

        AuthResponse authResponse = new AuthResponse(
                "jwt-access-token",
                "Bearer"
        );

        when(authService.login(request))
                .thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                authController.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful",
                response.getBody().getMessage());
        assertEquals(authResponse,
                response.getBody().getData());

        verify(authService).login(request);
    }

    @Test
    void testLogoutUser() {

        String authorization = "Bearer jwt-access-token";

        doNothing()
                .when(authService)
                .logout(authorization);

        ResponseEntity<ApiResponse<Void>> response =
                authController.logout(authorization);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful",
                response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(authService).logout(authorization);
    }

//    @Test
//    void testChangePassword() {
//
//        String email = "astik@gmail.com";
//
//        ChangePasswordRequest request = new ChangePasswordRequest(
//                "oldPassword",
//                "newPassword"
//        );
//
//        when(userDetails.getUsername())
//                .thenReturn(email);
//
//        doNothing()
//                .when(authService)
//                .changePassword(email, request);
//
//        ResponseEntity<ApiResponse<Void>> response =
//                authController.changePassword(userDetails, request);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals("Password changed successfully",
//                response.getBody().getMessage());
//        assertNull(response.getBody().getData());
//
//        verify(authService).changePassword(email, request);
//    }
}