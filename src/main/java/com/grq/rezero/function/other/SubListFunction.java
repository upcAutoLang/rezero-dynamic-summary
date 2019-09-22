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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 提取子表信息方法
 */
public class SubListFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    public static final String SUBLIST_ENTITY = FUNCTION_VAR_COMMON_ENTITY;
    public static final String SUBLIST_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    private Logger LOG = LoggerFactory.getLogger(SubListFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_ENTITY2LIST_SUBLIST;
    }

    /**
     * 提取子表信息
     *
     * @param env
     * @param arg1
     * @param arg2
     * @return
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONObject entity = null;
        String expression = null;
        /**
         * 提取 env 内的 Aviatro 参数
         */
        try {
            entity = (JSONObject) FunctionUtils.getJavaObject(arg1, env);
            expression = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        if (entity == null) {
            LOG.error("传入 entity 为 null，返回空 JSONObject");
            return new AviatorRuntimeJavaType(new JSONObject(true));
        }

        // 提取数据中的子表信息
        JSONArray result = entity.getJSONArray(expression);
        if (result == null) {
            result = new JSONArray();
        }
        return new AviatorRuntimeJavaType(result);
    }
}
