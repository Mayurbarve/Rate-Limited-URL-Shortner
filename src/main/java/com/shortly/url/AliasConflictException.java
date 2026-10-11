package com.shortly.url;
public class AliasConflictException extends RuntimeException { public AliasConflictException(String code){super("Short code already exists: "+code);} }
