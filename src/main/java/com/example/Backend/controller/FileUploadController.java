package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.file.FileUploadResponseDTO;
import com.example.Backend.model.UploadedFile;
import com.example.Backend.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Foundation-level upload/download endpoints. Deliberately generic (no
 * "eventId" or "bannerFor" parameter yet) since Event Management isn't
 * implemented - later modules can call FileStorageService directly and
 * attach the resulting UploadedFile to their own entity.
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponseDTO> upload(@RequestParam("file") MultipartFile file) {
        UploadedFile stored = fileStorageService.store(file);
        return ApiResponse.success("File uploaded", FileUploadResponseDTO.from(stored));
    }

    // Public (see SecurityConfig permitAll for /api/files/download/**) so uploaded
    // images can be rendered directly in <img> tags without attaching a Bearer token.
    @GetMapping("/download/{storedFileName}")
    public ResponseEntity<Resource> download(@PathVariable String storedFileName) {
        UploadedFile metadata = fileStorageService.findMetadata(storedFileName);
        Resource resource = fileStorageService.loadAsResource(storedFileName);

        String contentType = metadata.getContentType() != null ? metadata.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFileName() + "\"")
                .body(resource);
    }
}
