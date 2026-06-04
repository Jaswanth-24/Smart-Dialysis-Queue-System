package com.dialysis.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * QueueBroadcaster
 *
 * Called by QueueService whenever queue state changes.
 * Pushes updates to all connected React clients over WebSocket (STOMP).
 *
 * Subscriptions on the frontend:
 *   /topic/queue/{centerId}           → full sorted queue list
 *   /topic/machine/{centerId}         → machine availability board
 *   /user/queue/patient/{patientId}   → personal turn alert popup
 */
@Component
@RequiredArgsConstructor
public class QueueBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    /** Broadcast full queue update to everyone watching this center */
    public void broadcastQueueUpdate(UUID centerId, Object queueSnapshot) {
        messagingTemplate.convertAndSend(
            "/topic/queue/" + centerId,
            queueSnapshot
        );
    }

    /** Broadcast machine board update to this center */
    public void broadcastMachineUpdate(UUID centerId, Object machineSnapshot) {
        messagingTemplate.convertAndSend(
            "/topic/machine/" + centerId,
            machineSnapshot
        );
    }

    /**
     * Send turn alert to a specific patient.
     * Patient's React app listens on /user/queue/patient/{patientId}
     * Message: { "message": "Your turn is approaching", "position": 2 }
     */
    public void sendTurnAlert(UUID patientId, Object alert) {
        messagingTemplate.convertAndSendToUser(
            patientId.toString(),
            "/queue/patient/" + patientId,
            alert
        );
    }
}
