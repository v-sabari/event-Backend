package com.example.Backend.repository;

import com.example.Backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByEventIdAndUserId(Long eventId, Long userId);

    List<Feedback> findByEventId(Long eventId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double findAverageRatingForEvent(@Param("eventId") Long eventId);
}
