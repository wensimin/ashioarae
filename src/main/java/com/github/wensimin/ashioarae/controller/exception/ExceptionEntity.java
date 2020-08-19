package com.github.wensimin.ashioarae.controller.exception;

/**
 * 报错信息entity
 */
public class ExceptionEntity {
    private String message;
    private ExceptionType type;

    public ExceptionEntity(String message, ExceptionType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExceptionType getType() {
        return type;
    }

    public void setType(ExceptionType type) {
        this.type = type;
    }
}
