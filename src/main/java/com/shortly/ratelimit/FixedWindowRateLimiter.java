package com.shortly.ratelimit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
@Component("fixed-window")
public class FixedWindowRateLimiter implements RateLimiter {
    private final StringRedisTemplate redis; public FixedWindowRateLimiter(StringRedisTemplate redis){this.redis=redis;}
    public Decision consume(String key,int capacity,double refill){long seconds=Math.max(1,(long)Math.ceil(capacity/refill));long window=Instant.now().getEpochSecond()/seconds;String k="rate:fixed:"+key+":"+window;Long n=redis.opsForValue().increment(k);if(n!=null&&n==1)redis.expire(k,seconds,TimeUnit.SECONDS);boolean ok=n!=null&&n<=capacity;return new Decision(ok,Math.max(0,capacity-(n==null?0:n)),ok?0:seconds-Instant.now().getEpochSecond()%seconds,seconds);}
}
