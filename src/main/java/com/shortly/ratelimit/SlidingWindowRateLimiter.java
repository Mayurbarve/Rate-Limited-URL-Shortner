package com.shortly.ratelimit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.List;
@Component("sliding-window")
public class SlidingWindowRateLimiter implements RateLimiter {
    private static final DefaultRedisScript<Long> SCRIPT=new DefaultRedisScript<>("local now=tonumber(ARGV[1]); local win=tonumber(ARGV[2]); redis.call('ZREMRANGEBYSCORE',KEYS[1],0,now-win); local n=redis.call('ZCARD',KEYS[1]); if n<tonumber(ARGV[3]) then redis.call('ZADD',KEYS[1],now,ARGV[4]); redis.call('PEXPIRE',KEYS[1],win); return 1 else return 0 end",Long.class);
    private final StringRedisTemplate redis; public SlidingWindowRateLimiter(StringRedisTemplate redis){this.redis=redis;}
    public Decision consume(String key,int capacity,double refill){long win=Math.max(1000,(long)(capacity/refill*1000));long now=System.currentTimeMillis();Long result=redis.execute(SCRIPT,List.of("rate:sliding:"+key),String.valueOf(now),String.valueOf(win),String.valueOf(capacity),now+":"+java.util.UUID.randomUUID());boolean ok=result!=null&&result==1;return new Decision(ok,ok?capacity-1:0,ok?0:1,1);}
}
