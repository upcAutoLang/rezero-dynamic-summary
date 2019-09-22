package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSON;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import org.apache.commons.lang3.ArrayUtils;

/**
 * <pre>
 *     任意维度 → 零维，即输入任意维度，输出 String；
 *     通常为固定的字符串，直接输出；
 * </pre>
 */
public class OutputStringChainUnit extends AbstractDimensionChainUnit<JSON, String>
        implements FunctionNameConstants, FunctionVariableConstants {
    public OutputStringChainUnit() {
    }

    public OutputStringChainUnit(String expression, String... aviatorArgs) {
        super(expression, aviatorArgs);
    }

    /**
     * 暂时不做任何处理，直接输出
     *
     * @param src 输入源数据
     * @return
     * @throws DynamicSummaryException
     */
    @Override
    public String doDimensionExec(JSON src) throws DynamicSummaryException {

        // 只取 aviatorArgs 的第一个值
        if (!ArrayUtils.isEmpty(aviatorArgs)) {
            return aviatorArgs[0];
        }
        String errorMsg = String.format("OutputStringChainUnit 只接受一个 Aviator 参数，当前传入参数数量为 [%d] 个", aviatorArgs.length);
        throw new DynamicSummaryException(errorMsg);
    }
}
