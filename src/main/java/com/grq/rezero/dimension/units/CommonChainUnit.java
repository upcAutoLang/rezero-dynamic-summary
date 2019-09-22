package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSONObject;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.expression.ExpressionParser;
import org.apache.commons.lang3.ArrayUtils;

/**
 * <pre>
 *     通用维度单元：
 *     一维 → 零维，即输入 JSONObject，输出 String；
 *
 *     <strong>注：只接受一个 Aviator 参数；</strong>
 * </pre>
 */
public class CommonChainUnit extends AbstractDimensionChainUnit<JSONObject, String> {
    public CommonChainUnit(String expression, String[] aviatorArgs) {
        super(expression, aviatorArgs);
    }

    @Override
    public String doDimensionExec(JSONObject src) throws DynamicSummaryException {
        // 只取 aviatorArgs 第一个值
        if (!ArrayUtils.isEmpty(aviatorArgs)) {
            return ExpressionParser.executeExpression(src, aviatorArgs[0]);
        }
        String errorMsg = String.format("CommonChainUnit 只接受一个 Aviator 参数，当前传入参数数量为 [%d] 个", aviatorArgs.length);
        throw new DynamicSummaryException(errorMsg);
    }
}
