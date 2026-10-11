package com.shortly.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest @Testcontainers
class TokenBucketConcurrencyTest {
    @Container static GenericContainer<?> redis=new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void redisProperties(DynamicPropertyRegistry r){r.add("spring.data.redis.host",redis::getHost);r.add("spring.data.redis.port",()->redis.getMappedPort(6379));r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired TokenBucketRateLimiter limiter; @Autowired StringRedisTemplate template;
    @Test void admitsExactlyCapacityUnderParallelLoad() throws Exception {
        template.getConnectionFactory().getConnection().serverCommands().flushDb();int capacity=50;int requests=300;AtomicInteger allowed=new AtomicInteger();ExecutorService pool=Executors.newFixedThreadPool(32);CountDownLatch start=new CountDownLatch(1);
        for(int i=0;i<requests;i++)pool.submit(()->{try{start.await();if(limiter.consume("concurrency-test",capacity,0.000001).allowed())allowed.incrementAndGet();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
        start.countDown();pool.shutdown();assertEquals(true,pool.awaitTermination(30,TimeUnit.SECONDS));assertEquals(capacity,allowed.get());
    }
}
