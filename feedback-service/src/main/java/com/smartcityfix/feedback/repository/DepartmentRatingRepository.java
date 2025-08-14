package com.smartcityfix.feedback.repository;

import com.smartcityfix.feedback.model.DepartmentRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRatingRepository extends JpaRepository<DepartmentRating, UUID> {

    @Query("SELECT dr FROM DepartmentRating dr ORDER BY dr.averageRating DESC")
    List<DepartmentRating> findAllOrderByAverageRatingDesc();
}