package com.shortly.code;

final class Base62 {
    private static final char[] ALPHABET="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private Base62() {}
    static String encode(long n) { if(n==0)return "0"; StringBuilder b=new StringBuilder(); while(n>0){b.append(ALPHABET[(int)(n%62)]);n/=62;} return b.reverse().toString(); }
}
