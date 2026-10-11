package com.shortly.web;
import com.shortly.url.AliasConflictException;
import com.shortly.url.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(UrlNotFoundException.class) ResponseEntity<?> notFound(RuntimeException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","not_found","message",e.getMessage()));}
    @ExceptionHandler(AliasConflictException.class) ResponseEntity<?> conflict(RuntimeException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","alias_conflict","message",e.getMessage()));}
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class}) ResponseEntity<?> badRequest(Exception e){return ResponseEntity.badRequest().body(Map.of("error","invalid_request","message",e.getMessage()==null?"Invalid request":e.getMessage()));}
}
