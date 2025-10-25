package com.beaus.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.beaus.auth.application.gateway.IJwtService;
import com.beaus.auth.domain.CustomUserDetails;
import com.beaus.auth.domain.User;
import com.beaus.auth.infrastructure.UserRepository;
import com.beaus.auth.model.ApiResponse;
import com.beaus.auth.model.LoginRequest;
import com.beaus.auth.model.RegisterRequest;
import com.beaus.auth.model.TokenResponse;

class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private IJwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("registerUser()")
    class RegisterUserTest {

        @Test
        @DisplayName("should return conflict when username is taken")
        void shouldReturnConflictIfUsernameExists() {
            RegisterRequest request = new RegisterRequest("existingUser", "test@mail.com", "pass");
            when(userRepository.existsByUsername("existingUser")).thenReturn(true);

            ApiResponse<Object> response = authService.registerUser(request);

            assertFalse(response.isSuccess());
            assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());
            assertEquals("Username already taken", response.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should save user and return success response")
        void shouldSaveUserSuccessfully() {
            RegisterRequest request = new RegisterRequest("newUser", "new@mail.com", "pass123");
            when(userRepository.existsByUsername("newUser")).thenReturn(false);

            ApiResponse<Object> response = authService.registerUser(request);

            assertTrue(response.isSuccess());
            assertEquals("User registered successfully.", response.getMessage());
            assertEquals(HttpStatus.OK.value(), response.getStatusCode());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTest {

        @Test
        @DisplayName("should authenticate and return token response")
        void shouldAuthenticateAndReturnToken() {
            LoginRequest request = new LoginRequest("john", "pass");

            Authentication mockAuth = mock(Authentication.class);
            User mockUser = new User();
            mockUser.setUsername("john");
            mockUser.setPassword("pass");
            mockUser.setUserRoles(Collections.emptySet());

            CustomUserDetails mockUserDetails = new CustomUserDetails(mockUser);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(userDetailsService.loadUserByUsername("john")).thenReturn(mockUserDetails);
            when(jwtService.generateToken(mockUserDetails)).thenReturn("mock-access-token");
            when(jwtService.generateRefreshToken(mockUserDetails)).thenReturn("mock-refresh-token");

            ApiResponse<TokenResponse> response = authService.login(request);

            assertEquals("mock-access-token", response.getData().getAccessToken());
            assertEquals("mock-refresh-token", response.getData().getRefreshToken());
            assertEquals("Bearer", response.getData().getTokenType());
        }
        @Test
        @DisplayName("should throw BadCredentialsException when authentication fails")
        void loginBadCredentials() {
            LoginRequest request = new LoginRequest("john", "wrongpass");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }

        @Test
        @DisplayName("should throw Exception for unexpected errors")
        void loginUnexpectedError(){
            LoginRequest request = new LoginRequest("john","pass");
            Authentication mockAuth = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(userDetailsService.loadUserByUsername(anyString()))
                    .thenThrow(new RuntimeException("DB down"));

            assertThrows(RuntimeException.class, () -> authService.login(request));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userDetailsService).loadUserByUsername(anyString());

        }
    }
}
