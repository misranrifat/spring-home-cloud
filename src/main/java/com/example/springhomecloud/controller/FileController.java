package com.example.springhomecloud.controller;

import com.example.springhomecloud.model.FileEntity;
import com.example.springhomecloud.security.UserDetailsImpl;
import com.example.springhomecloud.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/files/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("No files were uploaded");
            }

            List<String> uploadedFiles = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    FileEntity savedFile = fileStorageService.storeFile(file, userDetails.getUsername());
                    uploadedFiles.add(savedFile.getFileName());
                } catch (Exception e) {
                    logger.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
                    failedFiles.add(file.getOriginalFilename());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", !uploadedFiles.isEmpty());
            response.put("uploadedFiles", uploadedFiles);
            if (!failedFiles.isEmpty()) {
                response.put("failedFiles", failedFiles);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Upload failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getAllFiles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<FileEntity> filesPage = fileStorageService.getAllUserFiles(userDetails.getUsername(), 0);
        return ResponseEntity.ok(filesPage.getContent());
    }

    @GetMapping("/files/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            FileEntity fileEntity = fileStorageService.getFile(id, userDetails.getUsername());
            Resource resource = fileStorageService.loadFileAsResource(id, userDetails.getUsername());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileEntity.getSize()))
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to download file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/files/delete/{id}")
    public String deleteFile(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            fileStorageService.deleteFile(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete file", e);
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete file: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @GetMapping("/files/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            FileEntity fileEntity = fileStorageService.getFile(id, userDetails.getUsername());
            if (!fileEntity.getContentType().startsWith("image/")) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(id, userDetails.getUsername());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to load thumbnail", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/files/delete-all")
    public String deleteAllFiles(@AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = fileStorageService.deleteAllUserFiles(userDetails.getUsername());
            redirectAttributes.addFlashAttribute("message",
                    String.format("Successfully deleted all %d file(s)", deletedCount));
        } catch (Exception e) {
            logger.error("Failed to delete all files", e);
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete all files: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/files/delete-multiple")
    @ResponseBody
    public ResponseEntity<?> deleteMultipleFiles(@RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            int count = fileStorageService.deleteMultipleFiles(request.get("fileIds"), userDetails.getUsername());
            return ResponseEntity.ok().body(Map.of("deletedCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete files: " + e.getMessage());
        }
    }
}