package com.github.wensimin.ashioarae.controller.exception;

/**
 * 异常类型枚举
 */
public enum ExceptionType {
    /**
     * 给开发者展示的error message
     */
    error,
    /**
     * cookie的error
     */
    cookie,
    /**
     * 超时性质error
     * 客户端可进行重试
     */
    timeout
}
