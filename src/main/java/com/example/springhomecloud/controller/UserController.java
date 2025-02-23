package com.example.springhomecloud.controller;

import com.example.springhomecloud.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/account/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserAccount(userDetails.getUsername());
            return "redirect:/login?deleted";
        } catch (Exception e) {
            logger.error("Failed to delete account", e);
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete account: " + e.getMessage());
            return "redirect:/home";
        }
    }
}