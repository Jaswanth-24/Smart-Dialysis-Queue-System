package com.dialysis.dto.response;

import com.dialysis.enums.Role;

public record StaffResponse(
        java.util.UUID id,
        String name,
        String email,
        Role role
) {}