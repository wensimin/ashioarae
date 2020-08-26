package com.github.wensimin.ashioarae.controller.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;

/**
 * 异常捕获controller
 */
@ControllerAdvice
public class ExceptionController {
    Logger logger = LoggerFactory.getLogger(ExceptionController.class);


    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionEntity> exception(Exception exception) {
        // time out exception
        if (exception.getCause() instanceof SocketTimeoutException) {
            return exception(new AshiTimeoutException("time out"));
        }
        StringWriter errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        logger.error(errors.toString());
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), ExceptionType.error),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AshiException.class)
    public ResponseEntity<ExceptionEntity> exception(AshiException exception) {
        return new ResponseEntity<>(new ExceptionEntity(exception.getMessage(), exception.getType()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
