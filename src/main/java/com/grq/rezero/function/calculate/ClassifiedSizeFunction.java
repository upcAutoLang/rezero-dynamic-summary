package com.grq.rezero.function.calculate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.expression.ExpressionMatchUnit;
import com.grq.rezero.expression.ExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassifiedSizeFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    public static final String CLASSIFIEDSIZE_LIST = FUNCTION_VAR_COMMON_LIST;
    public static final String CLASSIFIEDSIZE_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    private Logger LOG = LoggerFactory.getLogger(ClassifiedSizeFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_LIST2STRING_CLASSIFIED_SIZE;
    }

    /**
     * 根据传入表达式，对 JSONArray 每个元素进行解析；对于解析后结果的集合，进行去重的数量统计
     *
     * @param env  包含 list(JSONArray), aviatorArgs(String)
     * @param arg1 list (JSONArray) - 输入 JSONArray，通常该值需要在之前进行分类；
     * @param arg2 aviatorArgs (String) - 可用于 ExpressionParser#doHandleFieldUnit 解析
     * @return
     * @see com.grq.rezero.expression.ExpressionParser#doHandleFieldUnit(JSONObject, ExpressionMatchUnit)
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String expression = null;

        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            expression = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        /**
         * 1. 如果传入的待统计字段为空，则直接返回 JSONArray 的数量；
         * 2. 否则，开始筛选；
         */
        Integer num = 0;
        if (StringUtils.isEmpty(expression)) {
            num = CollectionUtils.isEmpty(list)
                    ? 0 : list.size();
            return new AviatorDecimal(num);
        }
        // 用于统计的集合
        Set<String> summarySet = new HashSet<>();
        if (CollectionUtils.isEmpty(list)) {
            num = CollectionUtils.isEmpty(list)
                    ? 0 : list.size();
        } else {
            for (Object o : list) {
                JSONObject e = (JSONObject) o;
                String field = ExpressionParser.doHandleFieldUnit(e, ExpressionMatchUnit.buildFieldUnit(expression));
                if (field != null) {
                    summarySet.add(field);
                }
            }
            num = summarySet.size();
        }
        return new AviatorDecimal(num);
    }
}
