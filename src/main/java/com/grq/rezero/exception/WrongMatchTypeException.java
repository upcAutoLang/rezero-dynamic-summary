package com.grq.rezero.exception;

/**
 * 动态汇总统计单元类型错误，继承于 WrongMatchException
 * 在单元类型 ExpressionMatchType 匹配错误时，抛出该异常
 *
 * @see com.grq.rezero.expression.ExpressionMatchType
 * @see WrongMatchException
 */
public class WrongMatchTypeException extends WrongMatchException {
    public WrongMatchTypeException() {
    }

    public WrongMatchTypeException(String message) {
        super(message);
    }

    public WrongMatchTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
