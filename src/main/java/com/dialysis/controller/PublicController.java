package com.dialysis.controller;

import com.dialysis.dto.response.LiveQueueResponse;
import com.dialysis.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/")
@RequiredArgsConstructor
public class PublicController {

    private final QueueService queueService;

    @GetMapping("/live/{centerId}")
    public List<LiveQueueResponse> getLiveQueue(
            @PathVariable UUID centerId
    ){
        return queueService.getLiveQueue(centerId);
    }
}
