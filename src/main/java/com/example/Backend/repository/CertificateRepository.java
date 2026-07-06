package com.example.Backend.repository;

import com.example.Backend.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByRegistrationId(Long registrationId);

    Optional<Certificate> findByCertificateCode(String certificateCode);

    boolean existsByRegistrationId(Long registrationId);

    java.util.List<Certificate> findByRegistrationUserId(Long userId);
}
