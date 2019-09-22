package com.grq.rezero.exception;

/**
 * Aviator 表达式执行异常，继承于 DynamicSummaryException
 */
public class ExpressionSyntaxErrorException extends DynamicSummaryException {
    public ExpressionSyntaxErrorException() {
    }

    public ExpressionSyntaxErrorException(String message) {
        super(message);
    }

    public ExpressionSyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
