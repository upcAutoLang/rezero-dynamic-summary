package com.grq.rezero.expression;

import java.util.List;

/**
 * 表达式全部信息
 */
public class ExpressionInfo {
    /**
     * 表达式
     */
    private String expression;
    /**
     * 解析正则表达式之后的匹配结果
     */
    private List<ExpressionMatchUnit> matchUnits;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<ExpressionMatchUnit> getMatchUnits() {
        return matchUnits;
    }

    public void setMatchUnits(List<ExpressionMatchUnit> matchUnits) {
        this.matchUnits = matchUnits;
    }
}
