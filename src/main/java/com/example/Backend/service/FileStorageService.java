package com.example.Backend.service;

import com.example.Backend.model.UploadedFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    UploadedFile store(MultipartFile file);

    /** For server-generated files (e.g. PDF certificates) that don't arrive as a MultipartFile upload. */
    UploadedFile storeGenerated(byte[] content, String suggestedFileName, String contentType);

    Resource loadAsResource(String storedFileName);

    UploadedFile findMetadata(String storedFileName);
}
