package com.grq.rezero.function.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.expression.ExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 从输入 List 中提取指定字段内容，存入输出 JSONArray 中
 */
public class ListExtractFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    public static final String LISTEXTRACT_LIST = FUNCTION_VAR_COMMON_LIST;
    public static final String LISTEXTRACT_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    private Logger LOG = LoggerFactory.getLogger(ListExtractFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_LISTADVANCED_EXTRACTOR;
    }

    /**
     * 通过传入表达式，从 JSONArray 中提取指定字段的元素；
     * 通用字段为 Key，提取值为 Value，将所有元素置入 JSONArray 的输出中；
     *
     * @param env 包含 list(JSONArray), expression(String)
     * @param arg1 list(JSONArray) - 输入已经添加类型内码信息的 JSONArray
     * @param arg2 expression(String) - 筛选条件，根据传入字符串进行提取
     * @return
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String extractField = null;

        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            extractField = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("ListExtractFunction 格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        // 提取
        JSONArray result = new JSONArray();
        if (!CollectionUtils.isEmpty(list)) {
            for (Object obj : list) {
                JSONObject entity = (JSONObject) obj;
                String expressionOutput = ExpressionParser.executeExpression(entity, extractField);
                result.add(expressionOutput);
            }
        }
        return new AviatorRuntimeJavaType(result);
    }
}
