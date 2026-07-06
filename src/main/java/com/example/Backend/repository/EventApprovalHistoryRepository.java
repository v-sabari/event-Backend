package com.example.Backend.repository;

import com.example.Backend.model.EventApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventApprovalHistoryRepository extends JpaRepository<EventApprovalHistory, Long> {

    List<EventApprovalHistory> findByEventIdOrderByCreatedAtAsc(Long eventId);
}
