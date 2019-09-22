package com.grq.rezero.function.filter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.EmptyException;
import com.grq.rezero.expression.ExpressionMatchUnit;
import com.grq.rezero.expression.ExpressionParser;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过表达式，从 List 中匹配字段相等的元素
 * <pre>
 *     通过表达式，从 List 中匹配字段相等的元素；
 *     表达式通过 "|" 符号获取若干组匹配条件，每组匹配条件用 [...=...] 的形式匹配；
 *     解析处的每组表达式使用<strong>并集运算</strong>；
 * </pre>
 */
public class FieldEqualFilterFunction extends FilterFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    /**
     * 分隔符，用于表示筛选条件 Map 的 value 中，表示被分隔符隔开的多个字段，都是满足该字段的筛选条件；
     */
    public static final String FILTER_VALUE_SEPARATOR_REGEX = "\\|";
    public static final String FILTER_VALUE_SEPARATOR = "|";

    /**
     * <pre>
     *     正则表达式，用于解析筛选条件，前面 [] 括号中的内容为<strong>字段</strong>，后面 [] 括号中的内容为<strong>筛选条件</strong>；
     *     同时，筛选条件中以 <strong>"/</strong> 为分隔符，表示被分隔符隔开的多个字段，都是满足该字段筛选条件；
     * </pre>
     */
    public static final String FILTER_REGEX = "\\[([^)(=]*?)=([^)(=]*?)]";
    public static final Pattern FILTER_REGEX_PATTERN = Pattern.compile(FILTER_REGEX);

    /**
     * 定义 Aviator 表达式的传参
     */
    public static String FIELDEQUAL_LIST = FUNCTION_VAR_COMMON_LIST;
    public static String FIELDEQUAL_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;
    private Logger LOG = LoggerFactory.getLogger(FieldEqualFilterFunction.class);

    public static String conditionsToExpression(Map<String, String> conditions) {
        if (conditions == null) {
            throw new EmptyException("FieldEqualFilterFunction # conditionsToExpression 错误：传入空条件集合");
        }

        List<String> groups = new ArrayList<>();
        for (String key : conditions.keySet()) {
            StringBuilder builder = new StringBuilder();
            String valueString = conditions.get(key) == null
                    ? "" : conditions.get(key);
            String[] valueArray = valueString.split(FILTER_VALUE_SEPARATOR_REGEX);
            if (!ArrayUtils.isEmpty(valueArray)) {
                for (String value : valueArray) {
                    builder.append("[").append(key).append("=").append(value).append("]");
                }
            }
            groups.add(builder.toString());
        }
        return StringUtils.join(groups, null);
    }

    /**
     * 传入字符串形式筛选条件，将其转为 Map(String, String)；
     * 主要用于类内，将外面传来的 String 形式的筛选条件转为 Map 形式；
     * 如果返回结果中已经包含筛选条件，则在返回结果对应的值后面添加分隔符 "/" 与 v，代表 "/" 分割后数组的所有值都可以通过该字段的筛选；
     *
     * @param expression String 类型的筛选条件
     * @return
     */
    public static Map<String, String> expressionToConditions(String expression) {
        if (StringUtils.isEmpty(expression)) {
            throw new EmptyException("FieldEqualFilterFunction # expressionToConditions 错误：传入空表达式");
        }

        Map<String, String> result = new HashMap<>();

        /**
         * 遍历所有分组，并用正则表达式解析
         */
        Matcher matcher = FILTER_REGEX_PATTERN.matcher(expression);
        while (matcher.find()) {
            // 如果匹配结果 group != 2，即没有满足 k-v 格式，则跳过
            int groupCount = matcher.groupCount();
            if (groupCount == 2) {
                String k = matcher.group(1);
                String v = matcher.group(2);
                /**
                 * 如果返回结果中已经包含筛选字段，则在返回结果对应的值后面添加分隔符 "/" 与 v，代表 "/" 分割后数组的所有值都可以通过该字段的筛选；
                 */
                if (result.containsKey(k)) {
                    String v_temp = result.get(k);
                    v_temp = v_temp + FILTER_VALUE_SEPARATOR + v;
                    result.put(k, v_temp);
                } else {
                    result.put(k, v);
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return FUNCTION_NAME_LISTADVANCED_FIELDEQUAL;
    }

    /**
     * 通过表达式，从 JSONArray 中匹配字段相等的元素
     *
     * @param env 包含 list(JSONArray), expression(String)
     * @param arg1 list(JSONArray) - 输入添加了类型内码的 JSONArray
     * @param arg2 expression(String) - 筛选条件，根据传入字符串解析
     * @return 返回筛选后的结果 (JSONArray)
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String filterString = null;
        Map<String, String> filter = new HashMap<>();

        /**
         * 提取 env 内的 Aviator 参数
         */
        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            filterString = FunctionUtils.getStringValue(arg2, env);
            filter = expressionToConditions(filterString);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage(), e);
        }
        JSONArray result = new JSONArray();
        if (!CollectionUtils.isEmpty(list)) {
            for (Object obj : list) {
                JSONObject entity = (JSONObject) obj;
                if (match(entity, filter)) {
                    result.add(entity);
                }
            }
        }

        return new AviatorRuntimeJavaType(result);
    }

    private boolean match(JSONObject entity, Map<String, String> filter) {
        if (!MapUtils.isEmpty(filter)) {
            for (String key : filter.keySet()) {
                // 使用表达式解析方式（支持普通取值与关联信息取值），获取数据中的值
                String value = ExpressionParser.doHandleFieldUnit(entity, ExpressionMatchUnit.buildFieldUnit(key));
                Object filterValue = filter.get(key);
                // 如果筛选值与从数据中的值都为 null，属于匹配情况
                if (value == null && filterValue == null) {
                    return true;
                }

                List<String> valueList = (filterValue == null)
                        ? new ArrayList<>() : Arrays.asList(filterValue.toString().split(FILTER_VALUE_SEPARATOR_REGEX));
                // 如果筛选值与从数据中的值相等，属于匹配情况
                if (value != null && valueList.contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
