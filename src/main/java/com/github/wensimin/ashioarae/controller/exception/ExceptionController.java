package com.github.wensimin.ashioarae.controller.exception;

import com.github.wensimin.ashioarae.service.exception.AshiException;
import com.github.wensimin.ashioarae.service.exception.CookieExpireException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 异常捕获controller
 */
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionEntity> exception(Exception exception) {
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), ExceptionType.error),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AshiException.class)
    public ResponseEntity<ExceptionEntity> exception(AshiException exception) {
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), ExceptionType.error),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = CookieExpireException.class)
    public ResponseEntity<ExceptionEntity> exception(CookieExpireException exception) {
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), ExceptionType.cookie),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
