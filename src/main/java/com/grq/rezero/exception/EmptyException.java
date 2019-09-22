package com.grq.rezero.exception;

/**
 * 空内容异常
 */
public class EmptyException extends DynamicSummaryException {
    public EmptyException() {
    }

    public EmptyException(String message) {
        super(message);
    }

    public EmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
