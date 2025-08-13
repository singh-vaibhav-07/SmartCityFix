package com.smartcityfix.complaint.service;

import com.smartcityfix.complaint.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ComplaintService {

    ComplaintResponse createComplaint(ComplaintRequest request);

    ComplaintResponse getComplaintById(UUID id);

    Page<ComplaintResponse> searchComplaints(ComplaintSearchRequest request);

    ComplaintResponse assignComplaint(UUID id, AssignmentRequest request);

    ComplaintResponse updateStatus(UUID id, StatusUpdateRequest request);

    void deleteComplaint(UUID id);
}