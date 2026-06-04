package com.dialysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record MachineRequest(

        @NotNull(message = "Center ID is required")
        UUID centerId,

        @NotBlank(message = "Machine number is required")
        String machineNumber,

        LocalDate lastServiced
) {}
