package com.dialysis.dto.response;

import com.dialysis.enums.MachineStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MachineResponse(
        UUID id,
        UUID centerId,
        String centerName,
        String machineNumber,
        MachineStatus status,
        LocalDate lastServiced,
        Boolean isActive,
        LocalDateTime availableAt,
        LocalDateTime createdAt
) {}
