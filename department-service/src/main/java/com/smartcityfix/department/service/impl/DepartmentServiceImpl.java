package com.smartcityfix.department.service.impl;

import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.department.dto.*;
import com.smartcityfix.department.model.ComplaintCategory;
import com.smartcityfix.department.model.Department;
import com.smartcityfix.department.model.Location;
import com.smartcityfix.department.repository.DepartmentRepository;
import com.smartcityfix.department.service.DepartmentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Value("${app.routing.max-distance-km:5.0}")
    private double maxDistanceKm;

    @Value("${app.routing.use-fallback-department:true}")
    private boolean useFallbackDepartment;

    @Value("${app.routing.fallback-department-id:}")
    private String fallbackDepartmentId;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creating new department with name: {}", request.getName());

        try {
            // Check if department with same name already exists
            departmentRepository.findByName(request.getName())
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("Department with name " + request.getName() + " already exists");
                    });

            Department department = Department.builder()
                    .name(request.getName())
                    .categories(request.getCategories())
                    .zone(request.getZone())
                    .contactEmail(request.getContactEmail())
                    .contactPhone(request.getContactPhone())
                    .endpoint(request.getEndpoint())
                    .capacity(request.getCapacity())
                    .currentWorkload(0)
                    .location(mapToLocation(request.getLocation()))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Department savedDepartment = departmentRepository.save(department);
            log.info("Department created successfully with id: {}", savedDepartment.getId());

            return mapToDepartmentResponse(savedDepartment);
        } catch (IllegalArgumentException e) {
            log.error("Error creating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating department", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        log.info("Fetching department with id: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

            log.info("Department found: {}", department.getId());
            return mapToDepartmentResponse(department);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching department with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByName(String name) {
        log.info("Fetching department with name: {}", name);

        try {
            Department department = departmentRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "name", name));

            log.info("Department found: {}", department.getId());
            return mapToDepartmentResponse(department);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching department with name: {}", name, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(int page, int size) {
        log.info("Fetching all departments, page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<Department> departments = departmentRepository.findAll(pageable);

            log.info("Found {} departments", departments.getTotalElements());

            return departments.map(this::mapToDepartmentResponse);
        } catch (Exception e) {
            log.error("Error fetching departments", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByCategory(ComplaintCategory category) {
        log.info("Fetching departments by category: {}", category);

        try {
            List<Department> departments = departmentRepository.findByCategory(category);

            log.info("Found {} departments for category {}", departments.size(), category);

            return departments.stream()
                    .map(this::mapToDepartmentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching departments by category: {}", category, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        log.info("Updating department with id: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

            // Check if name is being changed and if new name already exists
            if (!department.getName().equals(request.getName())) {
                departmentRepository.findByName(request.getName())
                        .ifPresent(d -> {
                            throw new IllegalArgumentException("Department with name " + request.getName() + " already exists");
                        });
            }

            department.setName(request.getName());
            department.setCategories(request.getCategories());
            department.setZone(request.getZone());
            department.setContactEmail(request.getContactEmail());
            department.setContactPhone(request.getContactPhone());
            department.setEndpoint(request.getEndpoint());
            department.setCapacity(request.getCapacity());

            if (request.getLocation() != null) {
                department.setLocation(mapToLocation(request.getLocation()));
            }

            Department updatedDepartment = departmentRepository.save(department);
            log.info("Department updated successfully: {}", updatedDepartment.getId());

            return mapToDepartmentResponse(updatedDepartment);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating department with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        log.info("Deleting department with id: {}", id);

        try {
            if (!departmentRepository.existsById(id)) {
                throw new ResourceNotFoundException("Department", "id", id);
            }

            departmentRepository.deleteById(id);
            log.info("Department deleted successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting department with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "departmentService", fallbackMethod = "routeComplaintFallback")
    @Retry(name = "departmentService")
    public RoutingResponse routeComplaint(RoutingRequest request) {
        log.info("Routing complaint with category: {}", request.getCategory());

        try {
            // Step 1: Find departments that handle this category
            List<Department> eligibleDepartments = departmentRepository.findByCategory(request.getCategory());

            if (eligibleDepartments.isEmpty()) {
                log.warn("No departments found for category: {}", request.getCategory());
                return getFallbackDepartment();
            }

            // Step 2: If zone is specified, filter by zone
            if (request.getZone() != null && !request.getZone().isEmpty()) {
                List<Department> zoneFilteredDepartments = eligibleDepartments.stream()
                        .filter(d -> request.getZone().equals(d.getZone()))
                        .collect(Collectors.toList());

                if (!zoneFilteredDepartments.isEmpty()) {
                    eligibleDepartments = zoneFilteredDepartments;
                }
            }

            // Step 3: If location is provided, filter by proximity
            if (request.getLocation() != null &&
                    request.getLocation().getLatitude() != null &&
                    request.getLocation().getLongitude() != null) {

                Location complaintLocation = mapToLocation(request.getLocation());

                // Filter departments by distance and sort by proximity
                List<Department> nearbyDepartments = eligibleDepartments.stream()
                        .filter(d -> d.getLocation() != null &&
                                d.getLocation().getLatitude() != null &&
                                d.getLocation().getLongitude() != null)
                        .filter(d -> d.getLocation().distanceTo(complaintLocation) <= maxDistanceKm)
                        .sorted(Comparator.comparingDouble(d -> d.getLocation().distanceTo(complaintLocation)))
                        .collect(Collectors.toList());

                if (!nearbyDepartments.isEmpty()) {
                    eligibleDepartments = nearbyDepartments;
                }
            }

            // Step 4: Choose department with lowest workload
            Department selectedDepartment = eligibleDepartments.stream()
                    .min(Comparator.comparing(d -> d.getCurrentWorkload() == null ? 0 : d.getCurrentWorkload()))
                    .orElse(null);

            if (selectedDepartment == null) {
                log.warn("Could not find suitable department for routing");
                return getFallbackDepartment();
            }

            // Step 5: Increment workload for selected department
            incrementWorkload(selectedDepartment.getId());

            log.info("Routed complaint to department: {}", selectedDepartment.getId());

            return RoutingResponse.builder()
                    .departmentId(selectedDepartment.getId())
                    .name(selectedDepartment.getName())
                    .endpoint(selectedDepartment.getEndpoint())
                    .contactEmail(selectedDepartment.getContactEmail())
                    .zone(selectedDepartment.getZone())
                    .build();
        } catch (Exception e) {
            log.error("Error routing complaint", e);
            throw e;
        }
    }

    public RoutingResponse routeComplaintFallback(RoutingRequest request, Exception e) {
        log.error("Fallback: Error routing complaint", e);
        return getFallbackDepartment();
    }

    @Override
    @Transactional
    public void incrementWorkload(UUID departmentId) {
        log.info("Incrementing workload for department: {}", departmentId);

        try {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

            department.incrementWorkload();
            departmentRepository.save(department);

            log.info("Workload incremented for department: {}, new workload: {}",
                    departmentId, department.getCurrentWorkload());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error incrementing workload for department: {}", departmentId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void decrementWorkload(UUID departmentId) {
        log.info("Decrementing workload for department: {}", departmentId);

        try {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

            department.decrementWorkload();
            departmentRepository.save(department);

            log.info("Workload decremented for department: {}, new workload: {}",
                    departmentId, department.getCurrentWorkload());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error decrementing workload for department: {}", departmentId, e);
            throw e;
        }
    }

    private RoutingResponse getFallbackDepartment() {
        if (!useFallbackDepartment || fallbackDepartmentId == null || fallbackDepartmentId.isEmpty()) {
            return null;
        }

        try {
            UUID id = UUID.fromString(fallbackDepartmentId);
            Department fallbackDepartment = departmentRepository.findById(id)
                    .orElse(null);

            if (fallbackDepartment != null) {
                incrementWorkload(fallbackDepartment.getId());

                return RoutingResponse.builder()
                        .departmentId(fallbackDepartment.getId())
                        .name(fallbackDepartment.getName())
                        .endpoint(fallbackDepartment.getEndpoint())
                        .contactEmail(fallbackDepartment.getContactEmail())
                        .zone(fallbackDepartment.getZone())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting fallback department", e);
        }

        return null;
    }

    private Location mapToLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        return Location.builder()
                .latitude(locationDto.getLatitude())
                .longitude(locationDto.getLongitude())
                .address(locationDto.getAddress())
                .build();
    }

    private LocationDto mapToLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .build();
    }

    private DepartmentResponse mapToDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .categories(department.getCategories())
                .zone(department.getZone())
                .contactEmail(department.getContactEmail())
                .contactPhone(department.getContactPhone())
                .endpoint(department.getEndpoint())
                .capacity(department.getCapacity())
                .currentWorkload(department.getCurrentWorkload())
                .location(mapToLocationDto(department.getLocation()))
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}