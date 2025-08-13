package com.smartcityfix.department.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "department_categories",
            joinColumns = @JoinColumn(name = "department_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Builder.Default
    private Set<ComplaintCategory> categories = new HashSet<>();

    @Column
    private String zone;

    @Column(nullable = false)
    private String contactEmail;

    @Column
    private String contactPhone;

    @Column
    private String endpoint;

    @Column
    private Integer capacity;

    @Column
    private Integer currentWorkload;

    @Embedded
    private Location location;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void incrementWorkload() {
        if (this.currentWorkload == null) {
            this.currentWorkload = 0;
        }
        this.currentWorkload++;
    }

    public void decrementWorkload() {
        if (this.currentWorkload != null && this.currentWorkload > 0) {
            this.currentWorkload--;
        }
    }
}