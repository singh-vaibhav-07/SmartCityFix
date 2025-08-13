package com.smartcityfix.complaint.repository;

import com.smartcityfix.complaint.model.Complaint;
import com.smartcityfix.complaint.model.ComplaintCategory;
import com.smartcityfix.complaint.model.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByCategory(ComplaintCategory category, Pageable pageable);

    Page<Complaint> findByStatusAndCategory(ComplaintStatus status, ComplaintCategory category, Pageable pageable);

    Page<Complaint> findByReportedBy(UUID reportedBy, Pageable pageable);

    Page<Complaint> findByAssignedTo(UUID assignedTo, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:reportedBy IS NULL OR c.reportedBy = :reportedBy) AND " +
            "(:assignedTo IS NULL OR c.assignedTo = :assignedTo)")
    Page<Complaint> findByFilters(
            @Param("status") ComplaintStatus status,
            @Param("category") ComplaintCategory category,
            @Param("reportedBy") UUID reportedBy,
            @Param("assignedTo") UUID assignedTo,
            Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
            "c.status = 'OPEN' AND " +
            "c.assignedTo IS NULL")
    List<Complaint> findUnassignedComplaints();
}