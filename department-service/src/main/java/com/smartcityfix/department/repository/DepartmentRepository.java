package com.smartcityfix.department.repository;

import com.smartcityfix.department.model.ComplaintCategory;
import com.smartcityfix.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByName(String name);

    @Query("SELECT d FROM Department d JOIN d.categories c WHERE c = :category")
    List<Department> findByCategory(@Param("category") ComplaintCategory category);

    @Query("SELECT d FROM Department d JOIN d.categories c WHERE c = :category AND d.zone = :zone")
    List<Department> findByCategoryAndZone(
            @Param("category") ComplaintCategory category,
            @Param("zone") String zone);

    @Query("SELECT d FROM Department d ORDER BY d.currentWorkload ASC")
    List<Department> findAllOrderByWorkloadAsc();

    @Query("SELECT d FROM Department d JOIN d.categories c WHERE c = :category ORDER BY d.currentWorkload ASC")
    List<Department> findByCategoryOrderByWorkloadAsc(@Param("category") ComplaintCategory category);
}