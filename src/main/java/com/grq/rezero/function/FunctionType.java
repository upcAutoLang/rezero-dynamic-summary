package com.grq.rezero.function;

public enum FunctionType {
    /**
     * 一维 → 零维：输入 JSONObject，输出 String
     */
    // 普通单元
    ENTITY2STRING_COMMON,

    /**
     * 二维 → 零维：输入 JSONArray，输出 String
     */
    // 累加方法
    LIST2STRING_SUMMARYBYFIELD,
    // 字符串合并
    LIST2STRING_JOIN,
    // 统计分类后每个列表的数量
    LIST2STRING_CLASSIFIED_SIZE,

    /**
     * 二维 → 一维：输入 JSONArray，输出 JSONObject
     */
    // 分类方法
    LIST2ENTITY_CLASSIFY,

    /**
     * 二维增强方法：输入 JSONArray，输出 JSONArray
     */
    // 匹配相等过滤器
    LISTADVANCED_FIELDEQUAL,
    // 关联信息匹配过滤器
    LISTADVANCED_RELATIONINFO,
    // 字段排序器
    LISTADVANCED_FIELDSORT,
    // 列表字段提取器
    LISTADVANCED_EXTRACTOR,

    /**
     * 一维增强方法：输入 JSONObject，输出 JSONObject
     */
    // 关联信息添加方法
    ENTITYADVANCED_RELATIONINFO,

    /**
     * 一维 → 二维：输入 JSONObject，输出 JSONArray
     */
    // 统计分类后的列表数据
    ENTITY2LIST_CLASSIFIED_SUMMARY,
    // 提取子表数据方法
    ENTITY2LIST_SUBLIST
}
