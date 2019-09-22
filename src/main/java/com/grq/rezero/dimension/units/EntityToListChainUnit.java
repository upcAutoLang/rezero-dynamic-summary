package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.EmptyException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     一维 → 二维，即输入 JSONObject，输出 JSONArray；
 *     通常是从 JSONObject 中提取出 JSONArray；
 *     传入表达式为 JSONObject 中的字段，且该字段需要转换为 JSONArray 格式，否则抛出异常；
 *
 *     <strong>注：只接受一个 Aviator 参数；</strong>
 * </pre>
 */
public class EntityToListChainUnit extends AbstractDimensionChainUnit<JSONObject, JSONArray> implements FunctionNameConstants, FunctionVariableConstants {
    public EntityToListChainUnit(String expression, String[] aviatorArgs) {
        super(expression, aviatorArgs);
    }

    @Override
    public JSONArray doDimensionExec(JSONObject src) throws DynamicSummaryException {
        // 构造参数
        Map<String, Object> env = new HashMap<>();
        env.put(FUNCTION_VAR_COMMON_ENTITY, src);

        // 只取 aviatorArgs 的第一个值
        if (!ArrayUtils.isEmpty(aviatorArgs)) {
            if (aviatorArgs.length == 1) {
                env.put(FUNCTION_VAR_COMMON_AVIATORARGS, aviatorArgs[0]);
            } else {
                env.put(FUNCTION_VAR_COMMON_AVIATORARGS, aviatorArgs);
            }
        }

        if (StringUtils.isEmpty(expression)) {
            throw new EmptyException("EntityToListChainUnit 传入表达式为空，直接返回 NULL");
        }

        Object result = null;
        try {
            result = AviatorEvaluator.execute(expression, env);
            return (JSONArray) result;
        } catch (ClassCastException e) {
            String resultClass = (result == null)
                    ? "NULL" : result.getClass().getSimpleName();
            String errorMsg = String.format("EntityToListChainUnit 类型转换错误，输入类型为 [%s]，输出类型为 [%s]", src.getClass().getName(), resultClass);
            throw new DynamicSummaryException(errorMsg, e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
