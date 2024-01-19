package me.ricky.flow.service;

import me.ricky.flow.EmbeddedRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Import(EmbeddedRedis.class) //
class UserQueueServiceTest {
    @Autowired
    private  UserQueueService userQueueService;
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();
        StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();
        StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void allowUser() {
    }

    @Test
    void isAllowed() {
    }
}