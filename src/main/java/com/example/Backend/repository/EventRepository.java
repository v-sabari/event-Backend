package com.example.Backend.repository;

import com.example.Backend.model.Event;
import com.example.Backend.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCreatedById(Long userId);

    List<Event> findByDepartmentId(Long departmentId);

    /**
     * Venue Double Booking Prevention: any event at the same venue, not in a
     * terminal/dead state, whose [startTime, endTime) overlaps the requested
     * window. excludeEventId lets an update check "everyone except myself".
     */
    @Query("""
            SELECT e FROM Event e
            WHERE e.venue.id = :venueId
              AND e.status NOT IN (com.example.Backend.model.EventStatus.REJECTED, com.example.Backend.model.EventStatus.CANCELLED)
              AND (:excludeEventId IS NULL OR e.id <> :excludeEventId)
              AND e.startTime < :endTime
              AND e.endTime > :startTime
            """)
    List<Event> findOverlappingBookings(@Param("venueId") Long venueId,
                                         @Param("startTime") Instant startTime,
                                         @Param("endTime") Instant endTime,
                                         @Param("excludeEventId") Long excludeEventId);

    /** Events visible to the public calendar / listing: published (and not yet completed/cancelled). */
    List<Event> findByStatusIn(List<EventStatus> statuses);

    /**
     * Search & Filters module: any combination of name/department/category/venue/date
     * may be omitted (null), in which case that filter is skipped. Always scoped to
     * PUBLISHED/COMPLETED events since this backs public-facing search.
     */
    @Query("""
            SELECT e FROM Event e
            WHERE e.status IN (com.example.Backend.model.EventStatus.PUBLISHED, com.example.Backend.model.EventStatus.COMPLETED)
              AND (:name IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:departmentId IS NULL OR e.department.id = :departmentId)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:venueId IS NULL OR e.venue.id = :venueId)
              AND (CAST(:date AS date) IS NULL OR CAST(e.startTime AS date) = :date)
            """)
    List<Event> search(@Param("name") String name,
                        @Param("departmentId") Long departmentId,
                        @Param("categoryId") Long categoryId,
                        @Param("venueId") Long venueId,
                        @Param("date") java.time.LocalDate date);

    long countByStatus(EventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE CAST(e.startTime AS date) = CURRENT_DATE")
    long countTodayEvents();

    List<Event> findByDepartmentIdIsNotNull();
}
