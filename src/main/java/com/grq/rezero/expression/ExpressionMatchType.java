package com.grq.rezero.expression;

import java.io.Serializable;

/**
 * 匹配结果类型
 */
public enum ExpressionMatchType implements Serializable {
    /**
     * 字段类型，在表达式中用 ${...}$ 格式表示
     */
    FIELD,
    /**
     * 方法类型，在表达式中用 #{...}# 格式表示，且表达式中支持方法的嵌套调用 (Aviator 特性)
     */
    FUNCTION
}
