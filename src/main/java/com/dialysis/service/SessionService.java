package com.dialysis.service;

import com.dialysis.entity.DialysisSession;
import com.dialysis.entity.Machine;
import com.dialysis.entity.QueueEntry;
import com.dialysis.entity.User;
import com.dialysis.enums.MachineStatus;
import com.dialysis.enums.QueueStatus;
import com.dialysis.enums.SessionStatus;
import com.dialysis.exception.BadRequestException;
import com.dialysis.exception.ResourceNotFoundException;
import com.dialysis.repository.DialysisSessionRepository;
import com.dialysis.repository.MachineRepository;
import com.dialysis.repository.QueueRepository;
import com.dialysis.repository.UserRepository;
import com.dialysis.websocket.QueueBroadcaster;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Transactional
@Service
public class SessionService {

    private final QueueRepository queueRepository;
    private final MachineRepository machineRepository;
    private final DialysisSessionRepository dialysisSessionRepository;
    private final UserRepository userRepository;
    private final QueueBroadcaster queueBroadcaster;
    private final QueueService queueService;

    public SessionService(
            QueueRepository queueRepository,
            MachineRepository machineRepository,
            DialysisSessionRepository dialysisSessionRepository,
            UserRepository userRepository,
            QueueBroadcaster queueBroadcaster,
            @Lazy QueueService queueService
    ) {
        this.queueRepository = queueRepository;
        this.machineRepository = machineRepository;
        this.dialysisSessionRepository = dialysisSessionRepository;
        this.userRepository = userRepository;
        this.queueBroadcaster = queueBroadcaster;
        this.queueService = queueService;
    }


    public void assignMachine(UUID queueId) {

        QueueEntry entry = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));

        if (entry.getStatus() != QueueStatus.WAITING) {
            throw new BadRequestException("Only WAITING patients can be assigned a machine");
        }

        Machine machine = machineRepository
                .findFirstByCenterIdAndStatusAndIsActiveTrue(
                        entry.getCenter().getId(),
                        MachineStatus.AVAILABLE
                )
                .orElseThrow(() -> new BadRequestException("No machine available"));

        machine.setStatus(MachineStatus.ASSIGNED);
        entry.setMachine(machine);
        entry.setStatus(QueueStatus.ASSIGNED);

        machineRepository.save(machine);
        queueRepository.save(entry);

        // Bug 2 fix: broadcast ASSIGNED state change
        queueBroadcaster.broadcastQueueUpdate(
                entry.getCenter().getId(),
                queueService.getLiveQueue(entry.getCenter().getId())
        );
        queueBroadcaster.broadcastMachineUpdate(
                entry.getCenter().getId(),
                machine.getStatus()
        );
    }


    public void startSession(UUID queueId) {

        QueueEntry entry = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));

        if (entry.getStatus() != QueueStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Patient must be in ASSIGNED status before starting session"
            );
        }

        if (entry.getMachine() == null) {
            throw new BadRequestException("No machine assigned to this patient");
        }

        // Bug 5 fix: extract authenticated technician from JWT security context
        String technicianEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User technician = userRepository.findByEmail(technicianEmail)
                .orElse(null);  // graceful — session still starts even if lookup fails

        Integer sessionCount = dialysisSessionRepository
                .countByPatientId(entry.getPatient().getId());

        DialysisSession session = DialysisSession.builder()
                .patient(entry.getPatient())
                .machine(entry.getMachine())
                .technician(technician)          // Bug 5 fix: now populated
                .sessionDate(LocalDate.now())
                .startTime(LocalDateTime.now())
                .status(SessionStatus.IN_PROGRESS)
                .sessionNumber(sessionCount + 1)
                .build();

        entry.setStatus(QueueStatus.IN_SESSION);

        queueRepository.save(entry);
        dialysisSessionRepository.save(session);

        // Bug 2 fix: broadcast IN_SESSION state change
        queueBroadcaster.broadcastQueueUpdate(
                entry.getCenter().getId(),
                queueService.getLiveQueue(entry.getCenter().getId())
        );
    }


    public void completeSession(UUID sessionId) {

        DialysisSession session = dialysisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Session is not active");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        Machine machine = session.getMachine();
        machine.setStatus(MachineStatus.CLEANING);
        machine.setAvailableAt(LocalDateTime.now().plusMinutes(20));

        QueueEntry queueEntry = queueRepository
                .findByPatientIdAndQueueDate(
                        session.getPatient().getId(),
                        LocalDate.now()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));

        queueEntry.setStatus(QueueStatus.COMPLETED);

        dialysisSessionRepository.save(session);
        machineRepository.save(machine);
        queueRepository.save(queueEntry);

        // Bug 2 fix: broadcast COMPLETED + machine now CLEANING
        queueBroadcaster.broadcastQueueUpdate(
                queueEntry.getCenter().getId(),
                queueService.getLiveQueue(queueEntry.getCenter().getId())
        );
        queueBroadcaster.broadcastMachineUpdate(
                queueEntry.getCenter().getId(),
                machine.getStatus()
        );
    }
}
