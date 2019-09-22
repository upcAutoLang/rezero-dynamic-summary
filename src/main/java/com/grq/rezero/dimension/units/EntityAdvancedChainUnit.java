package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.EmptyException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     一维增强维度单元 (一维 → 一维)，即输入 JSONObject，输出 JSONObject；
 *
 *     <strong>注：只接受一个 Aviator 参数；</strong>
 * </pre>
 */
public class EntityAdvancedChainUnit extends AbstractDimensionChainUnit<JSONObject, JSONObject>
        implements FunctionNameConstants, FunctionVariableConstants {
    public EntityAdvancedChainUnit(String expression, String[] aviatorArgs) {
        super(expression, aviatorArgs);
    }

    @Override
    public JSONObject doDimensionExec(JSONObject src) throws DynamicSummaryException {
        if (null == src) {
            throw new EmptyException("EntityAdvancedChainUnit 处理错误：传入原始数据为 null");
        }
        // 构造参数
        Map<String, Object> env = new HashMap<>();
        env.put(FUNCTION_VAR_COMMON_ENTITY, src);

        // 只取 aviatorArgs 的第一个值
        if (!ArrayUtils.isEmpty(aviatorArgs)) {
            env.put(FUNCTION_VAR_COMMON_AVIATORARGS, aviatorArgs[0]);
        } else {
            String errorMsg = String.format("EntityAdvancedChainUnit 只接受一个 Aviator 参数，当前传入参数数量为 %d", aviatorArgs.length);
            throw new DynamicSummaryException(errorMsg);
        }

        Object result = null;
        try {
            result = AviatorEvaluator.execute(expression, env);
            return (JSONObject) result;
        } catch (ClassCastException e) {
            String resultClass = (result == null)
                    ? "NULL" : result.getClass().getSimpleName();
            String errorMsg = String.format("EntityAdvancedChainUnit 类型转换错误，输入类型为 [%s]，输出类型为 [%s]", src.getClass().getName(), resultClass);
            throw new DynamicSummaryException(errorMsg, e);
        } catch (DynamicSummaryException e) {
            e.printStackTrace();
        }
        return null;
    }
}
