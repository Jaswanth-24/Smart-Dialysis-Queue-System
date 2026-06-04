package com.dialysis.repository;

import com.dialysis.entity.Machine;
import com.dialysis.enums.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<Machine, UUID> {


    Optional<Machine>
    findFirstByCenterIdAndStatusAndIsActiveTrue(
            UUID centerId,
            MachineStatus status
    );


    List<Machine>
    findByStatusAndAvailableAtBefore(
            MachineStatus status,
            LocalDateTime time
    );

    List<Machine>
    findByCenterId(UUID centerId);

    Optional<Machine> findByCenterIdAndMachineNumber(UUID centerId, String machineNumber);


    Optional<Machine> findByCenterIdAndMachineNumberAndIsActiveTrue(UUID centerId, String machineNumber);
    int countByCenterIdAndIsActiveTrue(UUID centerId);
}
