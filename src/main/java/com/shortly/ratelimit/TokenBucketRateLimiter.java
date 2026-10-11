package com.shortly.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("token-bucket")
public class TokenBucketRateLimiter implements RateLimiter {
    private static final DefaultRedisScript<List> SCRIPT=new DefaultRedisScript<>("""
        local now=tonumber(ARGV[1]); local capacity=tonumber(ARGV[2]); local rate=tonumber(ARGV[3]);
        local state=redis.call('HMGET',KEYS[1],'tokens','ts'); local tokens=tonumber(state[1]); local ts=tonumber(state[2]);
        if tokens==nil then tokens=capacity; ts=now end
        tokens=math.min(capacity,tokens+math.max(0,now-ts)*rate); local allowed=0; local retry=0;
        if tokens>=1 then tokens=tokens-1; allowed=1 else retry=math.ceil((1-tokens)/rate) end
        redis.call('HSET',KEYS[1],'tokens',tokens,'ts',now); redis.call('EXPIRE',KEYS[1],math.max(1,math.ceil(capacity/rate)*2));
        return {allowed,math.floor(tokens),retry,math.ceil((capacity-tokens)/rate)}
        """,List.class);
    private final StringRedisTemplate redis;
    public TokenBucketRateLimiter(StringRedisTemplate redis){this.redis=redis;}
    public Decision consume(String key,int capacity,double refill){List<?> result=redis.execute(SCRIPT,List.of("rate:"+key),String.valueOf(System.currentTimeMillis()/1000.0),String.valueOf(capacity),String.valueOf(refill));if(result==null||result.size()<4)throw new IllegalStateException("Rate limiter unavailable");return new Decision(Long.parseLong(result.get(0).toString())==1,Long.parseLong(result.get(1).toString()),Long.parseLong(result.get(2).toString()),Long.parseLong(result.get(3).toString()));}
}
