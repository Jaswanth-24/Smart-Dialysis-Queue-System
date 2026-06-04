package com.dialysis.dto.response;

import com.dialysis.enums.QueueStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record QueueCheckInResponse(

        UUID queueId,

        Integer tokenNumber,

        Double priorityScore,

        QueueStatus status,

        String patientName,

        String message

) {}