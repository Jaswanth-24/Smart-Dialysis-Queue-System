package com.dialysis.controller;

import com.dialysis.dto.request.PatientRegisterRequest;
import com.dialysis.dto.response.PatientRegisterResponse;
import com.dialysis.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    @PostMapping("/register")
    public PatientRegisterResponse register(@Valid @RequestBody PatientRegisterRequest request){
        return patientService.register(request);
    }
}
