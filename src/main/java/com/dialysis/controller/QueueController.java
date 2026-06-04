package com.dialysis.controller;

import com.dialysis.dto.request.QueueCheckInRequest;
import com.dialysis.dto.response.LiveQueueResponse;
import com.dialysis.dto.response.QueueCheckInResponse;
import com.dialysis.service.QueueService;
import com.dialysis.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public QueueCheckInResponse checkIn(
            @RequestBody QueueCheckInRequest request
    ) {
        return queueService.checkIn(request);
    }


    @GetMapping("/live/{centerId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'TECHNICIAN', 'NURSE', 'DOCTOR')")
    public List<LiveQueueResponse> getLiveQueue(
            @PathVariable UUID centerId
    ) {
        return queueService.getLiveQueue(centerId);
    }


    @PatchMapping("/{queueId}/cancel")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<String> cancelQueue(
            @PathVariable UUID queueId
    ) {
        queueService.cancelQueue(queueId);
        return ResponseEntity.ok("Queue cancelled successfully");
    }
}
