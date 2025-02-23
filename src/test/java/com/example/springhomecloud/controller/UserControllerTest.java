package com.example.springhomecloud.controller;

import com.example.springhomecloud.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private UserController userController;

    private static final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    void deleteAccount_Success() {
        // When
        String result = userController.deleteAccount(userDetails, redirectAttributes);

        // Then
        verify(userService).deleteUserAccount(TEST_USERNAME);
        assertEquals("redirect:/login?deleted", result);
    }

    @Test
    void deleteAccount_Failure() {
        // Given
        String errorMessage = "Database error";
        doThrow(new RuntimeException(errorMessage))
                .when(userService)
                .deleteUserAccount(TEST_USERNAME);

        // When
        String result = userController.deleteAccount(userDetails, redirectAttributes);

        // Then
        verify(userService).deleteUserAccount(TEST_USERNAME);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to delete account: " + errorMessage);
        assertEquals("redirect:/home", result);
    }
}
