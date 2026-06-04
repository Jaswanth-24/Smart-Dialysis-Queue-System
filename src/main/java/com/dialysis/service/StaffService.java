package com.dialysis.service;

import com.dialysis.dto.request.RegisterStaffRequest;
import com.dialysis.entity.Center;
import com.dialysis.entity.User;
import com.dialysis.enums.Role;
import com.dialysis.exception.ResourceNotFoundException;
import com.dialysis.repository.CenterRepository;
import com.dialysis.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final CenterRepository centerRepository;

    public String registerStaff(@Valid RegisterStaffRequest request) {
        Center center = centerRepository.findById(request.centerId())
                .orElseThrow(() -> new ResourceNotFoundException("Center not found"));
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadCredentialsException("Email Already Exists");
        }
        User user = User.builder()
                .center(center)
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .isActive(true)
                .build();

        userRepository.save(user);
        return "Staff account created successfully";

    }
}
