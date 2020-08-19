package com.github.wensimin.ashioarae.service.exception;

/**
 * cookie过期异常
 */
public class CookieExpireException extends AshiException {
    public CookieExpireException(String message) {
        super(message);
    }

    public CookieExpireException() {
        super("cookie过期");
    }
}
