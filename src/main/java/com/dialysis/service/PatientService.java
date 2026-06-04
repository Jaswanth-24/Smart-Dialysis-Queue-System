package com.dialysis.service;

import com.dialysis.dto.request.PatientRegisterRequest;
import com.dialysis.dto.response.PatientRegisterResponse;
import com.dialysis.entity.Center;
import com.dialysis.entity.Patient;
import com.dialysis.entity.User;
import com.dialysis.enums.Role;
import com.dialysis.exception.BadRequestException;
import com.dialysis.exception.ResourceNotFoundException;
import com.dialysis.repository.CenterRepository;
import com.dialysis.repository.PatientRepository;
import com.dialysis.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final CenterRepository centerRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientRegisterResponse register(@Valid PatientRegisterRequest request) {

        // ── Check existing email ───────────────────────────────
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadCredentialsException("Email already exists");
        }

        // ── Fetch center ───────────────────────────────────────
        Center center = centerRepository.findById(request.centerId())
                .orElseThrow(() -> new ResourceNotFoundException("Center not found"));

        // ── Fetch doctor (optional) ────────────────────────────
        User doctor = null;

        if (request.doctorId() != null) {
            doctor = userRepository.findById(request.doctorId())
                    .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

            if (doctor.getRole() != Role.DOCTOR) {
                throw new BadRequestException("Assigned user is not a doctor");
            }
        }

        // ── Create User ────────────────────────────────────────
        User user = User.builder()
                .center(center)
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // ── Create Patient ─────────────────────────────────────
        Patient patient = Patient.builder()
                .user(savedUser)
                .center(center)
                .doctor(doctor)
                .dateOfBirth(request.dateOfBirth())
                .bloodGroup(request.bloodGroup())
                .diagnosis(request.diagnosis())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .build();

        Patient savedPatient = patientRepository.save(patient);

        // ── Response ───────────────────────────────────────────
        return PatientRegisterResponse.builder()
                .id(savedPatient.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .message("Patient registered successfully")
                .build();
    }
}