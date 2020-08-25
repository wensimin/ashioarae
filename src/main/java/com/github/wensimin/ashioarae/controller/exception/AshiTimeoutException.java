package com.github.wensimin.ashioarae.controller.exception;

/**
 * 超时异常
 */
public class AshiTimeoutException extends AshiException {

    public AshiTimeoutException(String message) {
        super(message, ExceptionType.timeout);
    }
}
