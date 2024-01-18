package me.ricky.flow.controller;

import lombok.RequiredArgsConstructor;
import me.ricky.flow.dto.RegisterUserResponse;
import me.ricky.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
    private final UserQueueService userQueueService;

    @PostMapping
    public Mono<RegisterUserResponse> registerWaitQueue(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                        @RequestParam(name = "user_id") Long userId) {
        return userQueueService.registerWaitQueue(queue, userId)
                .map(RegisterUserResponse::new);
    }
}
