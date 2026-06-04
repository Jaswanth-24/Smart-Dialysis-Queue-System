package com.dialysis.service;

import com.dialysis.entity.Machine;
import com.dialysis.entity.QueueEntry;
import com.dialysis.enums.MachineStatus;
import com.dialysis.enums.QueueStatus;
import com.dialysis.repository.MachineRepository;
import com.dialysis.repository.QueueRepository;
import com.dialysis.websocket.QueueBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineSchedulerService {

    private final MachineRepository machineRepository;
    private final QueueRepository queueRepository;
    private final SessionService sessionService;
    private final QueueBroadcaster queueBroadcaster;
    private final QueueService queueService;

    @Scheduled(fixedRate = 60000)
    @Transactional   // Bug 1 fix: whole loop is one transaction — partial failures won't leave ghost states
    public void releaseMachines() {

        List<Machine> machines =
                machineRepository.findByStatusAndAvailableAtBefore(
                        MachineStatus.CLEANING,
                        LocalDateTime.now()
                );

        for (Machine machine : machines) {

            machine.setStatus(MachineStatus.AVAILABLE);
            machine.setAvailableAt(null);
            machineRepository.save(machine);

            List<QueueEntry> waitingPatients =
                    queueRepository.findByCenterIdAndStatusOrderByTokenNumberAsc(
                            machine.getCenter().getId(),
                            QueueStatus.WAITING
                    );

            if (!waitingPatients.isEmpty()) {
                QueueEntry next = waitingPatients.get(0);
                sessionService.assignMachine(next.getId());
            }

            // Bug 2 fix: broadcast updated queue and machine state after each release
            queueBroadcaster.broadcastQueueUpdate(
                    machine.getCenter().getId(),
                    queueService.getLiveQueue(machine.getCenter().getId())
            );
            queueBroadcaster.broadcastMachineUpdate(
                    machine.getCenter().getId(),
                    machine.getStatus()
            );
        }
    }
}