package com.grq.rezero.exception;

/**
 * 动态汇总统计匹配错误，继承于 DynamicSummaryException
 * 在通常的匹配错误发生时，抛出该异常
 *
 * @see com.grq.rezero.expression.ExpressionMatchType
 * @see DynamicSummaryException
 */
public class WrongMatchException extends DynamicSummaryException {
    public WrongMatchException() {
    }

    public WrongMatchException(String message) {
        super(message);
    }

    public WrongMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
