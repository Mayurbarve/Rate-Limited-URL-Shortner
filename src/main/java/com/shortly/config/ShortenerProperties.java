package com.shortly.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix="shortener")
public class ShortenerProperties {
    private String baseUrl = "http://localhost:8080";
    private String codeGenerator = "base62-counter";
    private String rateLimiter = "token-bucket";
    private Duration cacheTtl = Duration.ofMinutes(10);
    private String premiumApiKey = "";
    public String getBaseUrl(){return baseUrl;} public void setBaseUrl(String v){baseUrl=v;}
    public String getCodeGenerator(){return codeGenerator;} public void setCodeGenerator(String v){codeGenerator=v;}
    public String getRateLimiter(){return rateLimiter;} public void setRateLimiter(String v){rateLimiter=v;}
    public Duration getCacheTtl(){return cacheTtl;} public void setCacheTtl(Duration v){cacheTtl=v;}
    public String getPremiumApiKey(){return premiumApiKey;} public void setPremiumApiKey(String v){premiumApiKey=v;}
}
