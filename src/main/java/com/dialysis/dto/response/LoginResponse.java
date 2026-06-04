package com.dialysis.dto.response;

import com.dialysis.enums.Role;

public record LoginResponse(String token, String email, Role role) {
}
