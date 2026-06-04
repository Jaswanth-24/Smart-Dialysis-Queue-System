package com.dialysis.controller;

import com.dialysis.dto.request.RegisterStaffRequest;
import com.dialysis.dto.response.StaffResponse;
import com.dialysis.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/register")
    public String registerStaff(
            @Valid @RequestBody RegisterStaffRequest request
    ) {
        return staffService.registerStaff(request);
    }

}