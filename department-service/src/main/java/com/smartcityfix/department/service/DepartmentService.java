package com.smartcityfix.department.service;

import com.smartcityfix.department.dto.*;
import com.smartcityfix.department.model.ComplaintCategory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse getDepartmentById(UUID id);

    DepartmentResponse getDepartmentByName(String name);

    Page<DepartmentResponse> getAllDepartments(int page, int size);

    List<DepartmentResponse> getDepartmentsByCategory(ComplaintCategory category);

    DepartmentResponse updateDepartment(UUID id, DepartmentRequest request);

    void deleteDepartment(UUID id);

    RoutingResponse routeComplaint(RoutingRequest request);

    void incrementWorkload(UUID departmentId);

    void decrementWorkload(UUID departmentId);
}