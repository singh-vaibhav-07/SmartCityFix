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
public class ComplaintResolvedEvent extends BaseEvent {
    private UUID complaintId;
    private UUID departmentId;
    private String status;

    public ComplaintResolvedEvent(UUID complaintId, UUID departmentId, String status) {
        super("COMPLAINT_RESOLVED");
        this.complaintId = complaintId;
        this.departmentId = departmentId;
        this.status = status;
    }
}