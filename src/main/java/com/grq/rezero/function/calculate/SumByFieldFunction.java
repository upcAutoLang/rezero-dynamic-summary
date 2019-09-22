package com.grq.rezero.function.calculate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.EmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 加法函数表达式，用于 JSONArray 中不同元素相同字段（且为数值类型，否则抛出异常）的累加运算
 */
public class SumByFieldFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    // src(JSONArray) - 传入 JSONArray
    public static String SUMBYFIELD_LIST = FUNCTION_VAR_COMMON_LIST;
    // expression (String) - 待统计字段
    public static String SUMBYFIELD_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    private Logger LOG = LoggerFactory.getLogger(SumByFieldFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_LIST2STRING_SUMMARYBYFIELD;
    }

    /**
     * 通过表达式，从 JSONArray 中遍历所有元素，对待统计字段进行统计，返回统计结果
     *
     * @param env 包含 src(JSONArray), field(String)
     * @param arg1 src (JSONArray) - 输入 JSONArray
     * @param arg2 field (String) - 待统计字段，如果不为 Number 类型，则抛出错误
     * @return 返回统计后的结果，统计结果为 double 类型，需要前端转换数值格式；
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String field = null;
        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            field = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }
        /**
         * 遍历数据列表，对待统计字段进行统计
         */
        // 统计结果，先用浮点数进行计算
        double total = 0.0;
        if (!CollectionUtils.isEmpty(list)) {
            for (Object obj : list) {
                JSONObject entity = null;
                try {
                    entity = (JSONObject) obj;
                } catch (ClassCastException e) {
                    LOG.error("JSONArray 元素格式转换错误：[{}]", e.getMessage());
                    throw new DynamicSummaryException(e.getMessage());
                } catch (Exception e) {
                    LOG.error("未处理错误：[{}]", e.getMessage());
                    e.printStackTrace();
                    throw new DynamicSummaryException(e.getMessage(), e);
                }
                /**
                 * 获取待统计字段，并进行累加
                 * 如果待统计字段不是数值类型，则抛出错误
                 */
                Object value = entity.get(field);
                try {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    if (value instanceof Integer) {
                        int v = (int) value;
                        total += v;
                    } else if (value instanceof Double) {
                        double v = (double) value;
                        total += v;
                    }
                    // 其余情况，抛出异常
                    else {
                        throw new ClassCastException();
                    }
                } catch (NullPointerException e) {
                    String message = String.format("字段 [%s] 从数据中获取到的值为 NULL，是否未在字段前添加业务内码信息？", field);
                    LOG.error(message);
                    throw new EmptyException(message, e);
                } catch (ClassCastException e) {
                    String message = String.format("转换熟知错误，原数据类型为：[%s]，无法转为 Number 类型", value.getClass().getName());
                    LOG.error(message);
                    throw new DynamicSummaryException(message, e);
                } catch (Exception e) {
                    LOG.error("未处理错误：[{}]", e.getMessage());
                    e.printStackTrace();
                    throw new DynamicSummaryException(e.getMessage(), e);
                }
            }
        }
        /**
         * 统计结果统一用 Double 返回；
         * 如果累加值为 int 类型，需要在前端转换；
         */
        return new AviatorDouble(total);
    }
}
