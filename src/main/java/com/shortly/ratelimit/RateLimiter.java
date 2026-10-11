package com.shortly.ratelimit;
public interface RateLimiter { Decision consume(String key,int capacity,double refillPerSecond); record Decision(boolean allowed,long remaining,long retryAfterSeconds,long resetAfterSeconds){} }
