package com.shortly.ratelimit;

import com.shortly.config.ShortenerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String,RateLimiter> limiters; private final ShortenerProperties properties;
    public RateLimitFilter(Map<String,RateLimiter> limiters,ShortenerProperties properties){this.limiters=limiters;this.properties=properties;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
        String path=req.getRequestURI(); boolean create="POST".equals(req.getMethod())&&path.equals("/api/v1/urls");boolean redirect="GET".equals(req.getMethod())&&!path.startsWith("/api/")&&!path.startsWith("/actuator/")&&!path.startsWith("/swagger");
        if(!create&&!redirect){chain.doFilter(req,res);return;}
        String identity=req.getHeader("X-API-Key");if(identity==null||identity.isBlank())identity=req.getRemoteAddr();
        boolean premium=!properties.getPremiumApiKey().isBlank()&&properties.getPremiumApiKey().equals(req.getHeader("X-API-Key"));
        String tier=premium?"premium":"free"; int capacity=premium?60:(create?10:120);double refill=premium?10:(create?1:10);
        String endpoint=create?"create":"redirect";String limiterName=properties.getRateLimiter();RateLimiter limiter=limiters.get(limiterName);if(limiter==null)throw new IllegalStateException("Unknown rate limiter: "+limiterName);
        RateLimiter.Decision d=limiter.consume(endpoint+":"+tier+":"+identity,capacity,refill);
        res.setHeader("X-RateLimit-Limit",String.valueOf(capacity));res.setHeader("X-RateLimit-Remaining",String.valueOf(d.remaining()));res.setHeader("X-RateLimit-Reset",String.valueOf(d.resetAfterSeconds()));
        if(!d.allowed()){res.setHeader("Retry-After",String.valueOf(Math.max(1,d.retryAfterSeconds())));res.setStatus(429);res.setContentType("application/json");res.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");return;}chain.doFilter(req,res);
    }
}
