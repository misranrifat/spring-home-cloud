package com.example.springhomecloud.controller;

import com.example.springhomecloud.dto.JwtAuthResponse;
import com.example.springhomecloud.dto.SignInRequest;
import com.example.springhomecloud.model.User;
import com.example.springhomecloud.repository.UserRepository;
import com.example.springhomecloud.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RedirectAttributes redirectAttributes;
    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                authenticationManager,
                userRepository,
                passwordEncoder,
                tokenProvider);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // Act
        String result = authController.registerUser(user, redirectAttributes);

        // Assert
        assertEquals("redirect:/home", result);
        verify(userRepository).save(user);
        verify(passwordEncoder).encode("password123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void registerUser_EmailExists() {
        // Arrange
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        String result = authController.registerUser(user, redirectAttributes);

        // Assert
        assertEquals("redirect:/register", result);
        verify(redirectAttributes).addFlashAttribute("error", "Email already exists!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        SignInRequest signInRequest = new SignInRequest("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<?> response = authController.authenticateUser(signInRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        JwtAuthResponse jwtAuthResponse = (JwtAuthResponse) response.getBody();
        assertNotNull(jwtAuthResponse);
        assertEquals("jwt-token", jwtAuthResponse.getAccessToken());
        assertEquals("Bearer", jwtAuthResponse.getTokenType());
        assertEquals("test@example.com", jwtAuthResponse.getEmail());
        assertEquals("Test User", jwtAuthResponse.getFullName());
    }

    @Test
    void registerUser_Exception() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("Some error"));

        // Act
        String result = authController.registerUser(user, redirectAttributes);

        // Assert
        assertEquals("redirect:/register", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Registration failed:"));
        verify(userRepository, never()).save(any(User.class));
    }
}