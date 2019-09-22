package com.grq.rezero.constants;

/**
 * Aviator 表达式名称常量的定义
 */
public interface FunctionNameConstants {
    /**
     * 通用单元方法：输入 JSONObject，输出 String
     */
    // 加方法
    String FUNCTION_NAME_COMMON_ADD = "add";
    // 百分比计算方法
    String FUNCTION_NAME_COMMON_PERCENT = "percent";
    // 减方法
    String FUNCTION_NAME_COMMON_SUB = "sub";

    /**
     * 二维 → 零维：输入 JSONArray，输出 String
     */
    // 累加方法
    String FUNCTION_NAME_LIST2STRING_SUMMARYBYFIELD = "sumByField";
    // 字符串合并
    String FUNCTION_NAME_LIST2STRING_JOIN = "join";
    // 统计分类后每个列表的数量
    String FUNCTION_NAME_LIST2STRING_CLASSIFIED_SIZE = "classifiedSize";

    /**
     * 二维 → 一维：输入 JSONArray，输出 JSONObject
     */
    // 匹配相等过滤器
    String FUNCTION_NAME_LIST2ENTITY_CLASSIFY = "classify";

    /**
     * 二维增强方法：输入 JSONArray，输出 JSONArray
     */
    // 匹配相等过滤器
    String FUNCTION_NAME_LISTADVANCED_FIELDEQUAL = "fieldEqual";
    // 关联信息附加
    String FUNCTION_NAME_LISTADVANCED_RELATIONINFO = "relationInfo";
    // 关联信息匹配过滤器
    String FUNCTION_NAME_LISTADVANCED_RELATIONFILTER = "relationFilter";
    // 字段排序器
    String FUNCTION_NAME_LISTADVANCED_FIELDSORT = "fieldSort";
    // 列表字段提取器
    String FUNCTION_NAME_LISTADVANCED_EXTRACTOR = "extractor";

    /**
     * 一维增强方法：输入 JSONObject，输出 JSONObject
     */
    // 关联信息添加方法
    String FUNCTION_NAME_ENTITYADVANCED_RELATIONINFO = "relationInfo";

    /**
     * 一维 → 二维：输入 JSONObject，输出 JSONArray
     */
    // 统计分类后的列表数据
    String FUNCTION_NAME_ENTITY2LIST_CLASSIFIED_SUMMARY = "classifiedSummary";
    // 提取子表数据方法
    String FUNCTION_NAME_ENTITY2LIST_SUBLIST = "sublist";
}
