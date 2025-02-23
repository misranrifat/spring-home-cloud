package com.example.springhomecloud.service;

import com.example.springhomecloud.model.FileEntity;
import com.example.springhomecloud.model.User;
import com.example.springhomecloud.repository.FileRepository;
import com.example.springhomecloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Value("${spring.servlet.multipart.max-file-size:-1}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:-1}")
    private String maxRequestSize;

    @Value("${app.pagination.page-size:10}") // Default to 10 if not set
    private int pageSize;

    public FileStorageService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FileEntity storeFile(MultipartFile file, String userEmail) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            // Clean and validate filename
            String originalFilename = file.getOriginalFilename();
            String fileName = StringUtils.cleanPath(
                    originalFilename != null && !originalFilename.isEmpty() ? originalFilename : "unnamed_file");

            // Get content type with fallback
            String contentType = file.getContentType();
            contentType = (contentType != null && !contentType.isEmpty()) ? contentType : "application/octet-stream";

            // Get user
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create file entity
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setContentType(contentType);
            fileEntity.setSize(file.getSize());
            fileEntity.setUploadDateTime(LocalDateTime.now());
            fileEntity.setUser(user);

            // Read and set file data as byte array
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException("Failed to read file data");
            }
            fileEntity.setData(bytes);

            // Save and return
            logger.info("Storing file: {} ({} bytes) for user: {}",
                    fileName, bytes.length, userEmail);
            return fileRepository.save(fileEntity);

        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long fileId, String userEmail) {
        try {
            FileEntity fileEntity = getFile(fileId, userEmail);
            byte[] data = fileEntity.getData();
            if (data == null || data.length == 0) {
                throw new RuntimeException("File data is empty");
            }
            return new ByteArrayResource(data) {
                @Override
                public String getFilename() {
                    return fileEntity.getFileName();
                }
            };
        } catch (Exception e) {
            logger.error("Could not load file {}: {}", fileId, e.getMessage());
            throw new RuntimeException("Could not load file", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<FileEntity> getAllUserFiles(String email, int page) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FileEntity> filesPage = fileRepository.findByUser(user, pageable);

        // Add debug logging
        logger.info("Retrieved {} files for user {}", filesPage.getContent().size(), email);

        return filesPage;
    }

    public FileEntity getFile(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return fileRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void deleteFile(Long id, String email) {
        FileEntity file = getFile(id, email);
        fileRepository.delete(file);
        logger.info("File deleted: {}", file.getFileName());
    }

    public int deleteMultipleFiles(List<Long> fileIds, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileEntity> files = fileRepository.findByIdInAndUser(fileIds, user);
        fileRepository.deleteAll(files);

        logger.info("Deleted {} files for user: {}", files.size(), userEmail);
        return files.size();
    }

    public int deleteAllUserFiles(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileEntity> files = fileRepository.findByUser(user);
        fileRepository.deleteAll(files);

        logger.info("Deleted all {} files for user: {}", files.size(), email);
        return files.size();
    }

    public void saveFile(MultipartFile file, String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_file");
        fileEntity.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        fileEntity.setSize(file.getSize());
        fileEntity.setData(file.getBytes());
        fileEntity.setUser(user);
        fileEntity.setUploadDateTime(LocalDateTime.now());

        fileRepository.save(fileEntity);
        logger.info("File saved: {}", fileEntity.getFileName());
    }
}