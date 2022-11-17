package com.robosoft.lorem.exception;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandling
{
    @ExceptionHandler(value = ExpiredJwtException.class)
    public ResponseEntity<String> handleJWTToken(com.robosoft.lorem.exception.ExpiredJwtException e)
    {
        return new ResponseEntity<String>("Session has been Expired, Please login again", HttpStatus.BAD_REQUEST);

    }
}


