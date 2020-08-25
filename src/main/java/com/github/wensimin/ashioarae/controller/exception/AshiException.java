package com.github.wensimin.ashioarae.controller.exception;

/**
 * 同步异常
 */
public class AshiException extends RuntimeException {
    private final ExceptionType type;

    public AshiException(String message) {
        this(message, ExceptionType.error);
    }

    public AshiException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public ExceptionType getType() {
        return type;
    }
}
