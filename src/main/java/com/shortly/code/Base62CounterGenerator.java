package com.shortly.code;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
@Component("base62-counter")
public class Base62CounterGenerator implements ShortCodeGenerator {
    private final StringRedisTemplate redis;
    public Base62CounterGenerator(StringRedisTemplate redis){this.redis=redis;}
    @Override public String nextCode(String ignored){Long value=redis.opsForValue().increment("shortener:code-sequence"); if(value==null)throw new IllegalStateException("Could not allocate code"); return Base62.encode(value);}
}
