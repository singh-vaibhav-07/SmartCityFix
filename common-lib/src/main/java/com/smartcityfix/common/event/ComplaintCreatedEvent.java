package com.smartcityfix.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ComplaintCreatedEvent extends BaseEvent {
    private UUID complaintId;
    private String category;
    private LocationDto location;
    private UUID reportedBy;

    public ComplaintCreatedEvent(UUID complaintId, String category, LocationDto location, UUID reportedBy) {
        super("COMPLAINT_CREATED");
        this.complaintId = complaintId;
        this.category = category;
        this.location = location;
        this.reportedBy = reportedBy;
    }

    @Data
    @NoArgsConstructor
    public static class LocationDto {
        private double lat;
        private double lon;
        private String address;

        public LocationDto(double lat, double lon, String address) {
            this.lat = lat;
            this.lon = lon;
            this.address = address;
        }
    }
}