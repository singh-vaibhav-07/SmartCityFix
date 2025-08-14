package com.smartcityfix.feedback.repository;

import com.smartcityfix.feedback.model.Feedback;
import com.smartcityfix.feedback.model.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Page<Feedback> findByUserId(UUID userId, Pageable pageable);

    Page<Feedback> findByDepartmentId(UUID departmentId, Pageable pageable);

    Page<Feedback> findByDepartmentIdAndStatus(UUID departmentId, FeedbackStatus status, Pageable pageable);

    Optional<Feedback> findByComplaintIdAndUserId(UUID complaintId, UUID userId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.departmentId = :departmentId AND f.status = 'APPROVED'")
    Double findAverageRatingByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.departmentId = :departmentId AND f.status = 'APPROVED' AND f.rating = :rating")
    Integer countByDepartmentIdAndRating(@Param("departmentId") UUID departmentId, @Param("rating") Integer rating);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.departmentId = :departmentId AND f.status = 'APPROVED'")
    Integer countByDepartmentId(@Param("departmentId") UUID departmentId);

    List<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
}