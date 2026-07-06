package com.example.Backend.service.impl;

import com.example.Backend.model.AuditLog;
import com.example.Backend.repository.AuditLogRepository;
import com.example.Backend.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void record(String action, String entityType, Long entityId, String details) {
        AuditLog entry = new AuditLog();
        entry.setPerformedBy(resolveCurrentActor());
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        auditLogRepository.save(entry);
        log.debug("Audit: [{}] {} {} #{} - {}", entry.getPerformedBy(), action, entityType, entityId, details);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable) {
        return auditLogRepository.findByPerformedBy(performedBy, pageable);
    }

    @Override
    public Page<AuditLog> findByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    private String resolveCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "ANONYMOUS";
        }
        return authentication.getName();
    }
}
