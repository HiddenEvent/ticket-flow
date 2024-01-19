package me.ricky.flow.controller;

import lombok.RequiredArgsConstructor;
import me.ricky.flow.dto.AllowUserResponse;
import me.ricky.flow.dto.AllowedUserResponse;
import me.ricky.flow.dto.RegisterUserResponse;
import me.ricky.flow.service.UserQueueService;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                             @RequestParam(name = "count") Long count) {
        return userQueueService.allowUser(queue, count)
                .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowed(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                               @RequestParam(name = "user_id") Long userId) {
        return userQueueService.isAllowed(queue, userId)
                .map(AllowedUserResponse::new);
    }
}
