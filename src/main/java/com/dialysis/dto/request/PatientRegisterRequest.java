package com.dialysis.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record PatientRegisterRequest(

        UUID centerId,
        UUID doctorId,

        String name,
        String email,
        String password,

        LocalDate dateOfBirth,
        String bloodGroup,

        String diagnosis,

        String emergencyContactName,
        String emergencyContactPhone

) {}