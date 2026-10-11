package com.shortly.url;

import com.shortly.code.ShortCodeGenerator;
import com.shortly.config.ShortenerProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {
    private static final String MISS="__MISS__";
    private final UrlRepository urls; private final ClickEventRepository clicks; private final StringRedisTemplate redis;
    private final java.util.Map<String,ShortCodeGenerator> generators; private final ShortenerProperties properties;
    public UrlService(UrlRepository urls, ClickEventRepository clicks, StringRedisTemplate redis,
                      java.util.Map<String,ShortCodeGenerator> generators, ShortenerProperties properties) {
        this.urls=urls;this.clicks=clicks;this.redis=redis;this.generators=generators;this.properties=properties;
    }
    public UrlRecord create(String url,String alias,Instant expiry) {
        validateUrl(url); if(expiry!=null&&!expiry.isAfter(Instant.now()))throw new IllegalArgumentException("Expiry must be in the future");
        if(alias!=null&&!alias.matches("[A-Za-z0-9_-]{3,32}"))throw new IllegalArgumentException("Alias must be 3-32 URL-safe characters");
        ShortCodeGenerator generator=generators.get(properties.getCodeGenerator());if(generator==null)throw new IllegalStateException("Unknown code generator: "+properties.getCodeGenerator());
        for(int attempt=0;attempt<5;attempt++) {
            String code=alias==null?generator.nextCode(url):alias;
            try { UrlRecord saved=urls.saveAndFlush(new UrlRecord(code,url,Instant.now(),expiry)); redis.delete(cacheKey(code)); return saved; }
            catch(DataIntegrityViolationException ex){if(alias!=null)throw new AliasConflictException(code);}
        }
        throw new IllegalStateException("Could not generate a unique short code after 5 attempts");
    }
    @Transactional(readOnly=true)
    public Optional<UrlRecord> find(String code) {
        String key=cacheKey(code); String cached=redis.opsForValue().get(key);
        if(cached!=null){if(MISS.equals(cached))return Optional.empty();String[] fields=cached.split("\n",2);Instant expiry=fields.length>1&&!fields[1].isBlank()?Instant.parse(fields[1]):null;return Optional.of(new UrlRecord(code,fields[0],Instant.EPOCH,expiry));}
        Optional<UrlRecord> found=urls.findByShortCode(code);
        if(found.isPresent()){UrlRecord r=found.get();redis.opsForValue().set(key,r.getOriginalUrl()+"\n"+(r.getExpiresAt()==null?"":r.getExpiresAt().toString()),properties.getCacheTtl());}
        else redis.opsForValue().set(key,MISS,30,TimeUnit.SECONDS);
        return found;
    }
    @Async("analyticsExecutor") @Transactional
    public void recordClick(String code){Instant now=Instant.now();clicks.save(new ClickEvent(code,now));urls.findByShortCode(code).ifPresent(r->{r.setLastAccessedAt(now);urls.save(r);});}
    @Transactional(readOnly=true)
    public Stats stats(String code){UrlRecord record=urls.findByShortCode(code).orElseThrow(()->new UrlNotFoundException(code));return new Stats(code,clicks.countByCode(code),record.getLastAccessedAt());}
    private String cacheKey(String code){return "url:"+code;}
    private void validateUrl(String raw) {
        try {
            URI uri=URI.create(raw); if(!(uri.getScheme()!=null&&(uri.getScheme().equalsIgnoreCase("http")||uri.getScheme().equalsIgnoreCase("https")))||uri.getHost()==null)throw new IllegalArgumentException("Only absolute HTTP(S) URLs are allowed");
            String host=uri.getHost().toLowerCase(); if(host.equals("localhost")||host.endsWith(".localhost")||host.endsWith(".local"))throw new IllegalArgumentException("Private and localhost destinations are not allowed");
            for(InetAddress address:InetAddress.getAllByName(host)) if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||address.isSiteLocalAddress()||address.isMulticastAddress())throw new IllegalArgumentException("Private and localhost destinations are not allowed");
        } catch(IllegalArgumentException ex){throw ex;} catch(Exception ex){throw new IllegalArgumentException("URL host could not be resolved",ex);}
    }
    public record Stats(String shortCode,long clicks,Instant lastAccessedAt){}
}
