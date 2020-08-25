package com.github.wensimin.ashioarae.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.SocketTimeoutException;

/**
 * 异常捕获controller
 */
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionEntity> exception(Exception exception) {
        // time out exception
        if (exception.getCause() instanceof SocketTimeoutException) {
            return exception(new AshiTimeoutException("time out"));
        }
        exception.printStackTrace();
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), ExceptionType.error),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AshiException.class)
    public ResponseEntity<ExceptionEntity> exception(AshiException exception) {
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), exception.getType()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
