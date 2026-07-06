package com.example.Backend.service.impl;

import com.example.Backend.exception.FileStorageException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.UploadedFile;
import com.example.Backend.model.User;
import com.example.Backend.repository.UploadedFileRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Local-disk implementation of the file storage foundation. This is
 * intentionally the only class that touches the filesystem, so swapping to
 * S3/GCS later means implementing FileStorageService again, not hunting
 * down file I/O scattered across the app.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    // Kept intentionally permissive-but-bounded for a foundation module; the
    // Event module can extend this with per-feature rules (e.g. images only for banners).
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "application/pdf"
    );

    private final Path uploadRoot;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public FileStorageServiceImpl(@Value("${app.file.upload-dir}") String uploadDir,
                                   UploadedFileRepository uploadedFileRepository,
                                   UserRepository userRepository,
                                   AuditLogService auditLogService) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.uploadedFileRepository = uploadedFileRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;

        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory: " + this.uploadRoot);
        }
    }

    @Override
    @Transactional
    public UploadedFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );

        if (originalFileName.contains("..")) {
            throw new FileStorageException("Filename contains an invalid path sequence: " + originalFileName);
        }

        String storedFileName = buildStoredFileName(originalFileName);

        try {
            writeToDisk(storedFileName, file.getInputStream());
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFileName);
        }

        UploadedFile entity = new UploadedFile();
        entity.setOriginalFileName(originalFileName);
        entity.setStoredFileName(storedFileName);
        entity.setContentType(contentType);
        entity.setSizeBytes(file.getSize());

        currentUser().ifPresent(entity::setUploadedBy);

        UploadedFile saved = uploadedFileRepository.save(entity);
        log.info("File stored: {} ({} bytes) as {}", originalFileName, file.getSize(), storedFileName);
        auditLogService.record("FILE_UPLOADED", "UploadedFile", saved.getId(), "Uploaded " + originalFileName);
        return saved;
    }

    @Override
    @Transactional
    public UploadedFile storeGenerated(byte[] content, String suggestedFileName, String contentType) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot store empty generated content");
        }

        String storedFileName = buildStoredFileName(suggestedFileName);

        try {
            writeToDisk(storedFileName, new java.io.ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new FileStorageException("Failed to store generated file: " + suggestedFileName);
        }

        UploadedFile entity = new UploadedFile();
        entity.setOriginalFileName(suggestedFileName);
        entity.setStoredFileName(storedFileName);
        entity.setContentType(contentType);
        entity.setSizeBytes(content.length);

        currentUser().ifPresent(entity::setUploadedBy);

        UploadedFile saved = uploadedFileRepository.save(entity);
        log.info("Generated file stored: {} ({} bytes) as {}", suggestedFileName, content.length, storedFileName);
        auditLogService.record("FILE_GENERATED", "UploadedFile", saved.getId(), "Generated " + suggestedFileName);
        return saved;
    }

    private String buildStoredFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }
        return UUID.randomUUID() + extension;
    }

    private void writeToDisk(String storedFileName, java.io.InputStream content) throws IOException {
        Path target = uploadRoot.resolve(storedFileName).normalize();
        if (!target.getParent().equals(uploadRoot)) {
            throw new FileStorageException("Resolved path escapes the upload directory");
        }
        Files.copy(content, target);
    }

    @Override
    public Resource loadAsResource(String storedFileName) {
        try {
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (!filePath.getParent().equals(uploadRoot)) {
                throw new FileStorageException("Invalid file path requested");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + storedFileName);
        }
    }

    @Override
    public UploadedFile findMetadata(String storedFileName) {
        return uploadedFileRepository.findByStoredFileName(storedFileName)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + storedFileName));
    }

    private java.util.Optional<User> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return java.util.Optional.empty();
        }
        User principal = (User) authentication.getPrincipal();
        return userRepository.findById(principal.getId());
    }
}
