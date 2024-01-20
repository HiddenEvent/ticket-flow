package me.ricky.flow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class WaitingRoomController {
    @GetMapping("/waiting-room")
    Mono<Rendering> waitingRoom(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                @RequestParam(name = "user_id") Long userId) {
        return Mono.just(Rendering.view("waiting-room.html")
                .build());
    }
}
