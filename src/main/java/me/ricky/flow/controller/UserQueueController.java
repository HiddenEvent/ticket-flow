package me.ricky.flow.controller;

import lombok.RequiredArgsConstructor;
import me.ricky.flow.dto.AllowUserResponse;
import me.ricky.flow.dto.AllowedUserResponse;
import me.ricky.flow.dto.RankNumberResponse;
import me.ricky.flow.dto.RegisterUserResponse;
import me.ricky.flow.service.UserQueueService;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
                                               @RequestParam(name = "user_id") Long userId,
                                               @RequestParam(name = "token") String token
    ) {
        return userQueueService.isAllowedByToken(queue, userId, token)
                .map(AllowedUserResponse::new);
    }

    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRank(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                            @RequestParam(name = "user_id") Long userId) {
        return userQueueService.getRank(queue, userId)
                .map(RankNumberResponse::new);
    }

    @GetMapping("/touch")
    Mono<?> touch(@RequestParam(name = "queue", defaultValue = "default") String queue,
                  @RequestParam(name = "user_id") Long userId,
                  ServerWebExchange exchange) {
        return Mono.defer(() -> userQueueService.generateToken(queue, userId))
                .map(token -> {
                    exchange.getResponse().addCookie(
                            ResponseCookie
                                    .from("user-queue-%s-token".formatted(queue), token)
                                    .path("/")
                                    .maxAge(Duration.ofSeconds(300))
                                    .httpOnly(true)
                                    .secure(true)
                                    .build()
                    );
                    return token;
                })
                ;
    }
}
