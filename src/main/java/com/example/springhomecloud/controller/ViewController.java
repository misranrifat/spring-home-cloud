package com.example.springhomecloud.controller;

import com.example.springhomecloud.model.FileEntity;
import com.example.springhomecloud.model.User;
import com.example.springhomecloud.service.FileStorageService;
import com.example.springhomecloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public ViewController(FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping({ "/", "/home" })
    public String home(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        logger.info("Loading home page for user: {}, page: {}", userDetails.getUsername(), page);

        // Get user's full name
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("userFullName", user.getFullName());

        // Get files with pagination
        Page<FileEntity> filesPage = fileStorageService.getAllUserFiles(userDetails.getUsername(), page);

        // Add debug logging
        logger.info("Found {} files for user", filesPage.getContent().size());
        filesPage.getContent().forEach(file -> logger.info("File: {}, size: {}, uploaded: {}",
                file.getFileName(), file.getSize(), file.getUploadDateTime()));

        model.addAttribute("files", filesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", filesPage.getTotalPages());
        model.addAttribute("totalItems", filesPage.getTotalElements());

        return "home";
    }
}