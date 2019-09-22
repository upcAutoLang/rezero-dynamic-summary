package com.grq.rezero.expression;

import org.springframework.util.StringUtils;

/**
 * 表达式匹配内容，以列表形式保存在 ExpressionInfo 中
 *
 * @see ExpressionInfo
 */
public class ExpressionMatchUnit {
    /**
     * 在表达式中匹配到的字符串内容，包括前后括号字段
     */
    private String content;
    /**
     * 匹配类型
     */
    private ExpressionMatchType matchType;
    /**
     * 匹配内容在整个表达式中的起始位置，用于内容的替换（最小为 0）
     */
    private Integer begin;
    /**
     * 匹配内容在整个表达式中的结束位置，用于内容的替换（最大卫 expression.length）
     */
    private Integer end;

    public ExpressionMatchUnit(String content, ExpressionMatchType matchType, Integer begin, Integer end) {
        this.content = content;
        this.matchType = matchType;
        this.begin = begin;
        this.end = end;
    }

    /**
     * 获取默认的字段匹配单元（单例模式）
     *
     * @return
     */
    public static ExpressionMatchUnit getFieldInstance() {
        return Holder.defaultField;
    }

    /**
     * 获取默认的方法匹配单元（单例模式）
     *
     * @return
     */
    public static ExpressionMatchUnit getFunctionInstance() {
        return Holder.defaultFunction;
    }

    /**
     * 传入一个字符串内容，构建一个字符匹配单元
     *
     * @param src 字符匹配单元内部内容
     * @return 字符匹配单元
     */
    public static ExpressionMatchUnit buildFieldUnit(String src) {
        // 如果为空，直接返回默认字符匹配单元
        if (StringUtils.isEmpty(src)) {
            return getFieldInstance();
        }

        // 如果 content 满足字符匹配单元表达式，则不需要添加前后缀
        String content = (src.matches(ExpressionParser.FIELD_REGEX))
                ? src : ("${" + src + "}$");
        return new ExpressionMatchUnit(content, ExpressionMatchType.FIELD, 0, content.length());
    }

    /**
     * 传入一个字符串内容，构建一个方法匹配单元
     *
     * @param src 方法匹配单元内部内容
     * @return 方法匹配单元
     */
    public static ExpressionMatchUnit buildFunctionUnit(String src) {
        // 如果为空，直接返回默认方法匹配单元
        if (StringUtils.isEmpty(src)) {
            return getFunctionInstance();
        }

        // 如果 content 满足方法匹配单元表达式，则不需要添加前后缀
        String content = (src.matches(ExpressionParser.FUNCTION_REGEX))
                ? src : ("#{" + src + "}#");
        return new ExpressionMatchUnit(content, ExpressionMatchType.FUNCTION, 0, content.length());

    }

    /**
     * 将前后括号掐头去尾，截取匹配结果
     *
     * @return
     */
    public String matchResult() {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        switch (matchType) {
            case FIELD:
                return content.substring(2, content.length() - 2);
            case FUNCTION:
                return content.substring(2, content.length() - 2);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionMatchUnit that = (ExpressionMatchUnit) o;

        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (matchType != that.matchType) return false;
        if (begin != null ? !begin.equals(that.begin) : that.begin != null) return false;
        return end != null ? end.equals(that.end) : that.end == null;
    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (matchType != null ? matchType.hashCode() : 0);
        result = 31 * result + (begin != null ? begin.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ExpressionMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(ExpressionMatchType matchType) {
        this.matchType = matchType;
    }

    public Integer getBegin() {
        return begin;
    }

    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    /**
     * <pre>
     *     单例模式的静态内部类，持有 ExpressionMatchUnit 的实例，可以直接在类加载时初始化；
     *
     *     1. 调用 getFieldInstance() 和 getFunctionInstance() 方法时，如果作为静态类的 Holder 类还没有被初始化，则进行类初始化过程（即 clinit 方法），即实现了懒加载；
     *     2. 类初始化方法 clinit 是同步的，即保证了单例模式的同步性；
     * </pre>
     */
    private static class Holder {
        private static ExpressionMatchUnit defaultField
                = new ExpressionMatchUnit("${}$", ExpressionMatchType.FIELD, 0, 4);
        private static ExpressionMatchUnit defaultFunction
                = new ExpressionMatchUnit("#{}#", ExpressionMatchType.FUNCTION, 0, 4);
    }
}
