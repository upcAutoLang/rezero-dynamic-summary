package com.grq.rezero.dimension.units;

import com.grq.rezero.exception.DynamicSummaryException;

/**
 * 维度单元抽象类
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 */
public abstract class AbstractDimensionChainUnit<I, O> {
    /**
     * 表达式
     */
    protected String expression;
    /**
     * Aviator 表达式输入参数
     */
    protected String[] aviatorArgs;

    public AbstractDimensionChainUnit() {
    }

    public AbstractDimensionChainUnit(String expression, String[] aviatorArgs) {
        this.expression = expression;
        this.aviatorArgs = aviatorArgs;
    }

    /**
     * 维度处理，根据具体实现类，实现升维/降维/同维处理
     *
     * @param src 输入源数据
     * @return 输出
     * @throws DynamicSummaryException 处理过程中抛出异常
     */
    public abstract O doDimensionExec(I src) throws DynamicSummaryException;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String[] getAviatorArgs() {
        return aviatorArgs;
    }

    public void setAviatorArgs(String[] aviatorArgs) {
        this.aviatorArgs = aviatorArgs;
    }
}
