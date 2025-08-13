package com.smartcityfix.department.controller;

import com.smartcityfix.common.dto.ApiResponse;
import com.smartcityfix.department.dto.*;
import com.smartcityfix.department.model.ComplaintCategory;
import com.smartcityfix.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department Management", description = "APIs for department management and complaint routing")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create a new department", description = "Creates a new department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        log.info("Received request to create department: {}", request.getName());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Returns department details for the given ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable UUID id) {
        log.info("Fetching department with id: {}", id);
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get department by name", description = "Returns department details for the given name")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentByName(@PathVariable String name) {
        log.info("Fetching department with name: {}", name);
        DepartmentResponse response = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Returns all departments with pagination")
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all departments, page: {}, size: {}", page, size);
        Page<DepartmentResponse> response = departmentService.getAllDepartments(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get departments by category", description = "Returns departments that handle the specified category")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartmentsByCategory(@PathVariable ComplaintCategory category) {
        log.info("Fetching departments by category: {}", category);
        List<DepartmentResponse> response = departmentService.getDepartmentsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Updates an existing department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentRequest request) {
        log.info("Updating department with id: {}", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Deletes a department by ID")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        log.info("Deleting department with id: {}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.successNoData("Department deleted successfully"));
    }

    @PostMapping("/route")
    @Operation(summary = "Route complaint", description = "Routes a complaint to the best department based on category, location, and workload")
    public ResponseEntity<ApiResponse<RoutingResponse>> routeComplaint(@Valid @RequestBody RoutingRequest request) {
        log.info("Routing complaint with category: {}", request.getCategory());
        RoutingResponse response = departmentService.routeComplaint(request);

        if (response != null) {
            return ResponseEntity.ok(ApiResponse.success("Complaint routed successfully", response));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No suitable department found for routing"));
        }
    }

    @PostMapping("/workload")
    @Operation(summary = "Update department workload", description = "Increments or decrements the workload of a department")
    public ResponseEntity<ApiResponse<Void>> updateWorkload(@Valid @RequestBody WorkloadUpdateRequest request) {
        log.info("Updating workload for department: {}, operation: {}",
                request.getDepartmentId(), request.getOperation());

        if (request.getOperation() == WorkloadUpdateRequest.WorkloadOperation.INCREMENT) {
            departmentService.incrementWorkload(request.getDepartmentId());
        } else {
            departmentService.decrementWorkload(request.getDepartmentId());
        }

        return ResponseEntity.ok(ApiResponse.successNoData("Department workload updated successfully"));
    }
}