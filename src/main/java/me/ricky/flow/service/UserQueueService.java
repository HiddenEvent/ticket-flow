package me.ricky.flow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ricky.flow.exception.ErrorCode;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "user:queue:%s:wait";
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "user:queue:*:wait";
    private final String USER_QUEUE_PROCEED_KEY = "user:queue:%s:proceed";


    // 대기열 등록 메서드
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        // 자료형: sorted set 사용
        // - key: userId
        // - value: unix timestamp
        // rank
        var unixTimestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimestamp)
                .filter(isAdded -> isAdded)
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(isAdded -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : rank)
                ;
    }

    // 진입을 허용 메서드
    public Mono<Long> allowUser(final String queue, final Long count) {
        // 진입을 허용하는 단계
        // 1. wait queue 사용자를 제거
        // 2. proceed queue 사용자를 추가
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
                .flatMap(member -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), member.getValue(), Instant.now().getEpochSecond()))
                .count();
    }

    // 진입이 가능한 상태인지 조회
    public Mono<Boolean> isAllowed(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

    public Mono<Long> getRank(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    public void scheduleAllowUser() {
        log.info("called scheduling...");
        var maxAllowUserCount = 3L;
        // 사용자 접속 허용 처리
        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                        .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
                        .count(100)
                        .build()
                )
                .map(key -> key.split(":")[2])
                .flatMap(queue -> allowUser(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed)))
                .doOnNext(tuple -> log.info("maxAllowUserCount {} allowed {} users {}", maxAllowUserCount, tuple.getT2(), tuple.getT1()))
                .subscribe();

    }
}
