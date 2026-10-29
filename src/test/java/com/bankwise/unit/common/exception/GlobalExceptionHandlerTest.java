package com.bankwise.unit.common.exception;

import com.bankwise.common.exception.BusinessException;
import com.bankwise.common.exception.GlobalExceptionHandler;
import com.bankwise.common.exception.RagProcessingException;
import com.bankwise.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    }

    @Test
    void testBadCredentialsExceptionReturnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Invalid email or password");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void testAccessDeniedExceptionReturnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void testRagProcessingExceptionReturnsInternalServerErrorWithMessage() {
        RagProcessingException ex = new RagProcessingException("LLM returned an empty response");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRagProcessingException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LLM returned an empty response", response.getBody().getMessage());
    }

    @Test
    void testBusinessExceptionReturnsBadRequest() {
        BusinessException ex = new BusinessException("Invalid input");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }
}

