package com.shortly.code;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
@Component("hash-based")
public class HashCodeGenerator implements ShortCodeGenerator {
    public String nextCode(String url){try{byte[] digest=MessageDigest.getInstance("SHA-256").digest((url+":"+System.nanoTime()).getBytes(StandardCharsets.UTF_8));return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0,7);}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
