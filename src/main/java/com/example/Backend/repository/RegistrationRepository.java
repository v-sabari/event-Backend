package com.example.Backend.repository;

import com.example.Backend.model.Registration;
import com.example.Backend.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByEventIdAndUserIdAndStatusNot(Long eventId, Long userId, RegistrationStatus excludedStatus);

    Optional<Registration> findByQrToken(String qrToken);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    List<Registration> findByEventIdAndStatusOrderByRegisteredAtAsc(Long eventId, RegistrationStatus status);

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByUserId(Long userId);

    List<Registration> findByUserIdAndStatus(Long userId, RegistrationStatus status);

    List<Registration> findTop10ByOrderByRegisteredAtDesc();

    long countByStatus(RegistrationStatus status);
}
