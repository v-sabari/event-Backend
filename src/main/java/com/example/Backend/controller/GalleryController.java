package com.example.Backend.controller;

import com.example.Backend.dto.common.ApiResponse;
import com.example.Backend.dto.gallery.GalleryImageResponseDTO;
import com.example.Backend.model.GalleryImage;
import com.example.Backend.model.User;
import com.example.Backend.service.GalleryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @PostMapping(value = "/api/events/{eventId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<GalleryImageResponseDTO> addImage(@PathVariable Long eventId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(required = false) String caption,
                                                          @AuthenticationPrincipal User currentUser) {
        GalleryImage image = galleryService.addImage(eventId, file, caption, currentUser);
        return ApiResponse.success("Image added to gallery", GalleryImageResponseDTO.from(image));
    }

    @DeleteMapping("/api/gallery/{imageId}")
    @PreAuthorize("hasAnyRole('STUDENT_ORGANIZER', 'FACULTY_COORDINATOR', 'HOD', 'SUPER_ADMIN')")
    public ApiResponse<Void> removeImage(@PathVariable Long imageId, @AuthenticationPrincipal User currentUser) {
        galleryService.removeImage(imageId, currentUser);
        return ApiResponse.message("Image removed from gallery");
    }

    @GetMapping("/api/events/{eventId}/gallery")
    public ApiResponse<List<GalleryImageResponseDTO>> list(@PathVariable Long eventId) {
        List<GalleryImageResponseDTO> response = galleryService.findByEvent(eventId).stream()
                .map(GalleryImageResponseDTO::from).toList();
        return ApiResponse.success(response);
    }
}
