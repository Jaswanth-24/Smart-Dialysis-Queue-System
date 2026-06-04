package com.dialysis.dto.request;


import java.util.UUID;

public record QueueCheckInRequest(

        UUID patientId,

        Boolean emergency

) {}