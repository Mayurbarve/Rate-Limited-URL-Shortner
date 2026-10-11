package com.shortly.url;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="urls", indexes=@Index(name="idx_urls_short_code", columnList="short_code", unique=true))
public class UrlRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="short_code", nullable=false, unique=true, length=32) private String shortCode;
    @Column(name="original_url", nullable=false, length=4096) private String originalUrl;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="expires_at") private Instant expiresAt;
    @Column(name="last_accessed_at") private Instant lastAccessedAt;
    protected UrlRecord() {}
    public UrlRecord(String code, String url, Instant created, Instant expiry){shortCode=code;originalUrl=url;createdAt=created;expiresAt=expiry;}
    public Long getId(){return id;} public String getShortCode(){return shortCode;} public String getOriginalUrl(){return originalUrl;}
    public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;} public Instant getLastAccessedAt(){return lastAccessedAt;}
    public void setLastAccessedAt(Instant value){lastAccessedAt=value;}
}
