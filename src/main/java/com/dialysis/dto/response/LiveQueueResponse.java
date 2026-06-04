package com.dialysis.dto.response;

import com.dialysis.enums.MachineStatus;
import com.dialysis.enums.QueueStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record LiveQueueResponse(

        Integer tokenNumber,
        String patientName,
        QueueStatus status,
        String assignedMachine,   // null when WAITING
        Integer estimatedWaitMinutes
) {}