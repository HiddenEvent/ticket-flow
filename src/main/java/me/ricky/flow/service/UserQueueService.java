package me.ricky.flow.service;

import lombok.RequiredArgsConstructor;
import me.ricky.flow.exception.ErrorCode;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_KEY = "user:queue:%s:wait";

    // 대기열 등록 메서드
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        // 자료형: sorted set 사용
        // - key: userId
        // - value: unix timestamp
        // rank
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_KEY.formatted(queue), userId.toString(), unixTimestamp)
                .filter(isAdded -> isAdded)
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(isAdded -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_KEY.formatted(queue), userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : rank)
                ;
    }

}
