package com.dialysis.controller;

import com.dialysis.dto.request.MachineRequest;
import com.dialysis.dto.response.MachineResponse;
import com.dialysis.enums.MachineStatus;
import com.dialysis.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RECEPTIONIST', 'TECHNICIAN', 'NURSE')")
public class MachineController {

    private final MachineService machineService;


    @PostMapping
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<MachineResponse> addMachine(
            @Valid @RequestBody MachineRequest request
    ) {
        MachineResponse response = machineService.addMachine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/center/{centerId}")
    public ResponseEntity<List<MachineResponse>> getMachinesByCenter(
            @PathVariable UUID centerId
    ) {
        return ResponseEntity.ok(machineService.getMachinesByCenter(centerId));
    }


    @GetMapping("/{machineId}")
    public ResponseEntity<MachineResponse> getMachine(
            @PathVariable UUID machineId
    ) {
        return ResponseEntity.ok(machineService.getMachine(machineId));
    }


    @PatchMapping("/{machineId}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<MachineResponse> updateStatus(
            @PathVariable UUID machineId,
            @RequestParam MachineStatus status
    ) {
        return ResponseEntity.ok(machineService.updateStatus(machineId, status));
    }

    @PatchMapping("/{machineId}/deactivate")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<MachineResponse> deactivate(
            @PathVariable UUID machineId
    ) {
        return ResponseEntity.ok(machineService.deactivateMachine(machineId));
    }


    @PatchMapping("/{machineId}/activate")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<MachineResponse> activate(
            @PathVariable UUID machineId
    ) {
        return ResponseEntity.ok(machineService.activateMachine(machineId));
    }
}
