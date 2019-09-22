package com.grq.rezero.constants;

/**
 * 用于 Aviator 表达式中的变量
 */
public interface FunctionVariableConstants {
    /**
     * 通用变量
     */
    // 列表
    String FUNCTION_VAR_COMMON_LIST = "list";
    // 单条数据
    String FUNCTION_VAR_COMMON_ENTITY = "entity";
    // 字段
    String FUNCTION_VAR_COMMON_FIELD = "field";
    // 过滤器
    String FUNCTION_VAR_COMMON_FILTER = "filter";
    // Aviator 参数
    String FUNCTION_VAR_COMMON_AVIATORARGS = "aviatorArgs";
    // 通用目的字段名
    String FUNCTION_VAR_DEFAULT_TARGET_FIELD = "TARGET";

    /**
     * 计算方法
     */
    // 百分比方法
    String FUNCTION_VAR_PERCENT_CURVALUE = "current";
    String FUNCTION_VAR_PERCENT_TOTAL = "total";
}
