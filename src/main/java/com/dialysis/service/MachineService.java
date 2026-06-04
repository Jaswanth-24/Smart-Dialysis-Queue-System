package com.dialysis.service;

import com.dialysis.dto.request.MachineRequest;
import com.dialysis.dto.response.MachineResponse;
import com.dialysis.entity.Center;
import com.dialysis.entity.Machine;
import com.dialysis.enums.MachineStatus;
import com.dialysis.exception.BadRequestException;
import com.dialysis.exception.ResourceNotFoundException;
import com.dialysis.repository.CenterRepository;
import com.dialysis.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final CenterRepository centerRepository;

    // ── Add a new machine to a center ──────────────────────────────────────
    public MachineResponse addMachine(MachineRequest request) {

        Center center = centerRepository.findById(request.centerId())
                .orElseThrow(() -> new ResourceNotFoundException("Center not found"));

        machineRepository.findByCenterIdAndMachineNumberAndIsActiveTrue(center.getId(), request.machineNumber())
                .ifPresent(m -> {
                    throw new BadRequestException(
                            "Machine number '" + request.machineNumber() + "' already exists in this center"
                    );
                });

        Machine machine = Machine.builder()
                .center(center)
                .machineNumber(request.machineNumber())
                .status(MachineStatus.AVAILABLE)
                .lastServiced(request.lastServiced())
                .isActive(true)
                .build();

        Machine saved = machineRepository.save(machine);

        return toResponse(saved);
    }

    // ── List all machines for a center ─────────────────────────────────────
    public List<MachineResponse> getMachinesByCenter(UUID centerId) {

        centerRepository.findById(centerId)
                .orElseThrow(() -> new ResourceNotFoundException("Center not found"));

        return machineRepository.findByCenterId(centerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Get a single machine ───────────────────────────────────────────────
    public MachineResponse getMachine(UUID machineId) {

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));

        return toResponse(machine);
    }

    // ── Update machine status (manual override by staff) ──────────────────
    public MachineResponse updateStatus(UUID machineId, MachineStatus newStatus) {

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));

        machine.setStatus(newStatus);

        // Clear availableAt if staff manually makes it available or puts it in maintenance
        if (newStatus == MachineStatus.AVAILABLE
                || newStatus == MachineStatus.MAINTENANCE
                || newStatus == MachineStatus.OUT_OF_SERVICE) {
            machine.setAvailableAt(null);
        }

        Machine saved = machineRepository.save(machine);

        return toResponse(saved);
    }

    // ── Soft-deactivate a machine ──────────────────────────────────────────
    public MachineResponse deactivateMachine(UUID machineId) {

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));

        if (machine.getStatus() == MachineStatus.IN_USE) {
            throw new BadRequestException(
                    "Cannot deactivate a machine that is currently in use"
            );
        }

        machine.setIsActive(false);
        machine.setStatus(MachineStatus.OUT_OF_SERVICE);

        Machine saved = machineRepository.save(machine);

        return toResponse(saved);
    }

    // ── Re-activate a machine ──────────────────────────────────────────────
    public MachineResponse activateMachine(UUID machineId) {

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));

        machine.setIsActive(true);
        machine.setStatus(MachineStatus.AVAILABLE);
        machine.setAvailableAt(null);

        Machine saved = machineRepository.save(machine);

        return toResponse(saved);
    }

    // ── Count active+available machines for a center (used by queue ETA) ──
    public int countActiveMachines(UUID centerId) {
        return machineRepository.countByCenterIdAndIsActiveTrue(centerId);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────
    private MachineResponse toResponse(Machine machine) {
        return MachineResponse.builder()
                .id(machine.getId())
                .centerId(machine.getCenter().getId())
                .centerName(machine.getCenter().getName())
                .machineNumber(machine.getMachineNumber())
                .status(machine.getStatus())
                .lastServiced(machine.getLastServiced())
                .isActive(machine.getIsActive())
                .availableAt(machine.getAvailableAt())
                .createdAt(machine.getCreatedAt())
                .build();
    }
}
