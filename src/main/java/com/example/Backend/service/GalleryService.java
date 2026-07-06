package com.example.Backend.service;

import com.example.Backend.model.GalleryImage;
import com.example.Backend.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GalleryService {

    /** Reuses FileStorageService for the actual upload, then links the result to the event. */
    GalleryImage addImage(Long eventId, MultipartFile file, String caption, User uploader);

    void removeImage(Long imageId, User currentUser);

    List<GalleryImage> findByEvent(Long eventId);
}
