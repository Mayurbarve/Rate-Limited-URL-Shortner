package com.shortly.code;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
@Component("random-with-retry")
public class RandomCodeGenerator implements ShortCodeGenerator {
    private static final char[] ALPHABET="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final SecureRandom random=new SecureRandom();
    public String nextCode(String ignored){StringBuilder s=new StringBuilder(7);for(int i=0;i<7;i++)s.append(ALPHABET[random.nextInt(ALPHABET.length)]);return s.toString();}
}
