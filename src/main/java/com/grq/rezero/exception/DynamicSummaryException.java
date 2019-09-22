package com.grq.rezero.exception;

/**
 * 动态汇总统计通用异常，继承于 RuntimeException
 */
public class DynamicSummaryException extends RuntimeException {
    public DynamicSummaryException() {
    }

    public DynamicSummaryException(String message) {
        super(message);
    }

    public DynamicSummaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
