package com.dialysis.service;

import com.dialysis.dto.request.LoginRequest;
import com.dialysis.dto.response.LoginResponse;
import com.dialysis.entity.User;
import com.dialysis.repository.UserRepository;
import com.dialysis.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid email or password")
                );

        boolean matches = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );

        if (!matches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user, user.getRole().name(), user.getId().toString());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole()
        );
    }
}
