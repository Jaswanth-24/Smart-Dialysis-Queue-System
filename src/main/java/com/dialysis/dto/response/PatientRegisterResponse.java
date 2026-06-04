package com.dialysis.dto.response;

import lombok.Builder;

import java.util.UUID;
@Builder
public record PatientRegisterResponse(
        UUID id,
        String name,
        String email,
        String message
) {}
