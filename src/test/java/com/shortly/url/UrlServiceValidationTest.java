package com.shortly.url;

import com.shortly.config.ShortenerProperties;
import com.shortly.code.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlServiceValidationTest {
    @Test void rejectsNonHttpSchemesBeforePersistence() {
        UrlRepository urls=mock(UrlRepository.class);ClickEventRepository clicks=mock(ClickEventRepository.class);
        UrlService service=new UrlService(urls,clicks,mock(StringRedisTemplate.class),Map.of("base62-counter",mock(ShortCodeGenerator.class)),new ShortenerProperties());
        assertThrows(IllegalArgumentException.class,()->service.create("file:///etc/passwd",null,null));
        verifyNoInteractions(urls);
    }
}
