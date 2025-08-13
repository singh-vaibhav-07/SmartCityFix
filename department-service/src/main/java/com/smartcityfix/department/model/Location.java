package com.smartcityfix.department.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String address;

    public double distanceTo(Location other) {
        if (latitude == null || longitude == null ||
                other.latitude == null || other.longitude == null) {
            return Double.MAX_VALUE;
        }

        // Haversine formula to calculate distance between two points on Earth
        double earthRadius = 6371; // kilometers

        double dLat = Math.toRadians(other.latitude - latitude);
        double dLon = Math.toRadians(other.longitude - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }
}