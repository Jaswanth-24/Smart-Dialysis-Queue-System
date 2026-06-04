package com.dialysis.controller;

import com.dialysis.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Technician physically starts the session at the machine
    @PostMapping("/{queueId}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<String> startSession(
            @PathVariable UUID queueId
    ) {
        sessionService.startSession(queueId);
        return ResponseEntity.ok("Session started successfully");
    }

    // Technician marks session complete when dialysis is done
    @PatchMapping("/{sessionId}/complete")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<String> completeSession(
            @PathVariable UUID sessionId
    ) {
        sessionService.completeSession(sessionId);
        return ResponseEntity.ok("Session completed successfully");
    }
}
