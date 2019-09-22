package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSONArray;
import com.googlecode.aviator.AviatorEvaluator;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <pre>
 *     二维 → 零维，即输入 JSONArray，输出 String；
 *     通常为统计总结 JSONArray，输出 String 的方法执行；
 *
 *     <strong>注：只接受一个 Aviator 参数；</strong>
 * </pre>
 */
public class ListToStringChainUnit extends AbstractDimensionChainUnit<JSONArray, String>
        implements FunctionNameConstants, FunctionVariableConstants {
    /**
     * Aviator 方法解析正则表达式
     */
    public static final String AVIATOR_REGEX = "(\\w+)\\(([a-zA-Z0-9]+),([a-zA-Z0-9]+)\\)";
    public static final Pattern AVIATOR_REGEX_PATTERN = Pattern.compile(AVIATOR_REGEX);

    public ListToStringChainUnit(String expression, String[] aviatorArgs) {
        super(expression, aviatorArgs);
    }

    /**
     * 输入 JSONArray，经过一系列处理，输出 String；
     *
     * @param src 输入源数据
     * @return
     * @throws DynamicSummaryException
     */
    @Override
    public String doDimensionExec(JSONArray src) throws DynamicSummaryException {
        // 构造参数
        Map<String, Object> env = new HashMap<>();
        env.put(FUNCTION_VAR_COMMON_LIST, src);

        // 只取 aviatorArgs 的第一个值
        if (!ArrayUtils.isEmpty(aviatorArgs)) {
            if (aviatorArgs.length == 1) {
                env.put(FUNCTION_VAR_COMMON_AVIATORARGS, aviatorArgs[0]);
            } else {
                String errorMsg = String.format("ListToStringChainUnit 只接受一个 Aviator 参数，当前传入参数数量为 [%d] 个", aviatorArgs.length);
                throw new DynamicSummaryException(errorMsg);
            }
        }

        Object result = null;
        try {
            result = AviatorEvaluator.execute(expression, env);
            return result.toString();
        } catch (ClassCastException e) {
            String resultClass = (result == null)
                    ? "NULL" : result.getClass().getSimpleName();
            String errorMsg = String.format("ListToStringChainUnit 类型转换错误，输入类型为 [%s]，输出类型为 [%s]", src.getClass().getName(), resultClass);
            throw new DynamicSummaryException(errorMsg, e);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
