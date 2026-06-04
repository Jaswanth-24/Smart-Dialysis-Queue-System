package com.dialysis.dto.request;


import java.util.UUID;

public record RegisterStaffRequest(
        UUID centerId,
        String name,
        String email,
        String password,
        String role
) {}
