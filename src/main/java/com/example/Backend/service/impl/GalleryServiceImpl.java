package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Event;
import com.example.Backend.model.GalleryImage;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.model.UploadedFile;
import com.example.Backend.repository.GalleryImageRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.EventService;
import com.example.Backend.service.FileStorageService;
import com.example.Backend.service.GalleryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class GalleryServiceImpl implements GalleryService {

    private static final Logger log = LoggerFactory.getLogger(GalleryServiceImpl.class);

    private final GalleryImageRepository galleryImageRepository;
    private final FileStorageService fileStorageService;
    private final EventService eventService;
    private final AuditLogService auditLogService;

    public GalleryServiceImpl(GalleryImageRepository galleryImageRepository,
                               FileStorageService fileStorageService,
                               EventService eventService,
                               AuditLogService auditLogService) {
        this.galleryImageRepository = galleryImageRepository;
        this.fileStorageService = fileStorageService;
        this.eventService = eventService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public GalleryImage addImage(Long eventId, MultipartFile file, String caption, User uploader) {
        Event event = eventService.findById(eventId);
        UploadedFile uploadedFile = fileStorageService.store(file);

        GalleryImage image = new GalleryImage();
        image.setEvent(event);
        image.setUploadedFile(uploadedFile);
        image.setCaption(caption);
        image.setUploadedBy(uploader);

        GalleryImage saved = galleryImageRepository.save(image);
        log.info("Gallery image added to event {} by {}", eventId, uploader.getRegNumber());
        auditLogService.record("GALLERY_IMAGE_ADDED", "Event", eventId, "Image added by " + uploader.getRegNumber());
        return saved;
    }

    @Override
    @Transactional
    public void removeImage(Long imageId, User currentUser) {
        GalleryImage image = galleryImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery image not found with id: " + imageId));

        boolean isUploader = image.getUploadedBy().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.FACULTY_COORDINATOR;
        if (!isUploader && !isPrivileged) {
            throw new AccessDeniedCustomException("You cannot remove an image you did not upload");
        }

        galleryImageRepository.delete(image);
        log.info("Gallery image {} removed by {}", imageId, currentUser.getRegNumber());
        auditLogService.record("GALLERY_IMAGE_REMOVED", "GalleryImage", imageId, "Removed by " + currentUser.getRegNumber());
    }

    @Override
    public List<GalleryImage> findByEvent(Long eventId) {
        return galleryImageRepository.findByEventId(eventId);
    }
}
