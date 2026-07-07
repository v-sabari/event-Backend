package com.example.Backend.service.impl;

import com.example.Backend.exception.AccessDeniedCustomException;
import com.example.Backend.exception.ApiException;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.model.Certificate;
import com.example.Backend.model.Registration;
import com.example.Backend.model.RegistrationStatus;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.CertificateRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.CertificateService;
import com.example.Backend.service.FileStorageService;
import com.example.Backend.service.NotificationService;
import com.example.Backend.service.RegistrationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Automatic PDF Certificate: one PDF per ATTENDED registration, generated
 * on demand (idempotent - calling it again for the same registration just
 * throws a conflict rather than generating a duplicate).
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy").withZone(ZoneOffset.UTC);

    private final CertificateRepository certificateRepository;
    private final RegistrationService registrationService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  RegistrationService registrationService,
                                  FileStorageService fileStorageService,
                                  NotificationService notificationService,
                                  AuditLogService auditLogService) {
        this.certificateRepository = certificateRepository;
        this.registrationService = registrationService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public Certificate generateForRegistration(Long registrationId, User currentUser) {
        certificateRepository.findByRegistrationId(registrationId).ifPresent(c -> {
            throw new ApiException("A certificate has already been generated for this registration", HttpStatus.CONFLICT);
        });

        Registration registration = registrationService.findById(registrationId);

        // BOLA fix: currentUser was accepted but never checked, letting any
        // Student Organizer generate a certificate for any event. A Student
        // Organizer must have created the event; Faculty Coordinator/HOD/Super
        // Admin act as oversight roles and may act on any event.
        if (currentUser.getRole() == Role.STUDENT_ORGANIZER
                && !registration.getEvent().getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedCustomException("You can only generate certificates for events you created");
        }

        if (registration.getStatus() != RegistrationStatus.ATTENDED) {
            throw new ApiException("Certificates can only be generated for students marked as attended", HttpStatus.CONFLICT);
        }

        String certificateCode = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        byte[] pdfBytes = renderPdf(registration, certificateCode);

        var uploadedFile = fileStorageService.storeGenerated(
                pdfBytes,
                "certificate-" + registration.getId() + ".pdf",
                "application/pdf"
        );

        Certificate certificate = new Certificate();
        certificate.setRegistration(registration);
        certificate.setStoredFileName(uploadedFile.getStoredFileName());
        certificate.setCertificateCode(certificateCode);

        Certificate saved = certificateRepository.save(certificate);

        notificationService.notify(registration.getUser(),
                "Certificate Ready: " + registration.getEvent().getTitle(),
                "Your certificate of participation for '" + registration.getEvent().getTitle() + "' is ready to download.",
                "CERTIFICATE_READY", "Certificate", saved.getId(), true);

        log.info("Certificate generated for registration {} by {}", registrationId, currentUser.getRegNumber());
        auditLogService.record("CERTIFICATE_GENERATED", "Certificate", saved.getId(), "For registration " + registrationId);
        return saved;
    }

    @Override
    public Certificate findByRegistration(Long registrationId) {
        return certificateRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("No certificate found for registration id: " + registrationId));
    }

    private byte[] renderPdf(Registration registration, String certificateCode) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())); // landscape
            document.addPage(page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float pageWidth = page.getMediaBox().getWidth();
            float centerY = page.getMediaBox().getHeight() / 2;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                drawCentered(cs, titleFont, 28, "Certificate of Participation", pageWidth, centerY + 120);
                drawCentered(cs, bodyFont, 14, "This certifies that", pageWidth, centerY + 70);
                drawCentered(cs, titleFont, 22, registration.getUser().getName(), pageWidth, centerY + 40);
                drawCentered(cs, bodyFont, 14,
                        "successfully participated in \"" + registration.getEvent().getTitle() + "\"",
                        pageWidth, centerY + 5);
                drawCentered(cs, bodyFont, 12,
                        "held on " + DATE_FORMAT.format(registration.getEvent().getStartTime()),
                        pageWidth, centerY - 20);
                drawCentered(cs, bodyFont, 10, "Certificate ID: " + certificateCode, pageWidth, centerY - 80);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ApiException("Failed to generate certificate PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void drawCentered(PDPageContentStream cs, PDType1Font font, float fontSize, String text, float pageWidth, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}