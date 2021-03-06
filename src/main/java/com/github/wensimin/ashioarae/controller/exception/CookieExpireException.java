package com.github.wensimin.ashioarae.controller.exception;

/**
 * cookie过期异常
 */
public class CookieExpireException extends AshiException {
    public CookieExpireException(String message) {
        super(message, ExceptionType.cookie);
    }

    public CookieExpireException() {
        this("cookie 过期");
    }
}
