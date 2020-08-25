package com.github.wensimin.ashioarae.controller.exception;

public class AshiTimeoutException extends AshiException {

    public AshiTimeoutException(String message) {
        super(message, ExceptionType.timeout);
    }
}
