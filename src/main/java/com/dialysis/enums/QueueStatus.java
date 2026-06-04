package com.dialysis.enums;

public enum QueueStatus {
    WAITING,        // in queue, not yet called
    CALLED,         // turn notification sent
    ASSIGNED,       // machine reserved, waiting for technician to start the session
    IN_SESSION,     // dialysis in progress
    COMPLETED,      // session done
    CANCELLED,      // left / removed
    EMERGENCY       // emergency override active
}
