package com.shortly.url;

import com.shortly.config.ShortenerProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;

@RestController
public class UrlController {
    private final UrlService service; private final ShortenerProperties properties;
    public UrlController(UrlService service,ShortenerProperties properties){this.service=service;this.properties=properties;}
    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateResponse> create(@Valid @RequestBody CreateRequest request){UrlRecord r=service.create(request.url(),request.customAlias(),request.expiresAt());return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(r.getShortCode(),properties.getBaseUrl()+"/"+r.getShortCode(),r.getExpiresAt()));}
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode){UrlRecord r=service.find(shortCode).orElseThrow(()->new UrlNotFoundException(shortCode));if(r.getExpiresAt()!=null&&!r.getExpiresAt().isAfter(Instant.now()))return ResponseEntity.status(HttpStatus.GONE).build();service.recordClick(shortCode);return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(r.getOriginalUrl())).build();}
    @GetMapping("/api/v1/urls/{shortCode}/stats")
    public UrlService.Stats stats(@PathVariable String shortCode){return service.stats(shortCode);}
    public record CreateRequest(@NotBlank @Size(max=4096) String url,@Size(min=3,max=32) String customAlias,Instant expiresAt){}
    public record CreateResponse(String shortCode,String shortUrl,Instant expiresAt){}
}
