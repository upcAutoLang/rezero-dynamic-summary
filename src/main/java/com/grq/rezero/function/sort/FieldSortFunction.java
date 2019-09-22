package com.grq.rezero.function.sort;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.expression.ExpressionMatchUnit;
import com.grq.rezero.expression.ExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <pre>
 *     字段排序方法；
 *
 *     从表达式解析出排序规则，对输入的 List 进行排序；
 *
 *     排序字段的要求如下：
 *     1. 字段需要存在，如果不存在，排序时返回 Integer.MAX_VALUE;
 *     2. 获取的字段值为 String 类型，且满足数字格式；如果不满足，则排序时返回 Integer.MAX_VALUE;
 * </pre>
 */
public class FieldSortFunction extends AbstractSortFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    public static final String FIELDSORT_LIST = FUNCTION_VAR_COMMON_LIST;
    public static final String FIELDSORT_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    /**
     * 正则表达式，用于解析是否可以使用正则表达式进行比较；
     */
    public static final String FIELD_SORT_REGEX = "\\d+";
    public static final Pattern FIELD_SORT_REGEX_PATTERN = Pattern.compile(FIELD_SORT_REGEX);

    private Logger LOG = LoggerFactory.getLogger(FieldSortFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_LISTADVANCED_FIELDSORT;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String sortString = null;

        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            sortString = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        if (!CollectionUtils.isEmpty(list)) {
            // 构建排序器
            Comparator comparator = buildFieldComparator(sortString);
            list.sort(comparator);
        }
        return new AviatorRuntimeJavaType(list);
    }

    /**
     * 根据传入的表达式，构建比较器
     *
     * @param expression 表达式
     * @return
     */
    private Comparator buildFieldComparator(String expression) {
        return (o1, o2) -> {
            /**
             * 1. 判空
             */
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;

            /**
             * 2. 判断并转换类型
             */
            if (!(o1 instanceof JSONObject)) {
                LOG.error("排序内容类型为 [{}]，无法解析为 JSONObject 类型", o1.getClass().getName());
                return 1;
            }
            if (!(o2 instanceof JSONObject)) {
                LOG.error("排序内容类型为 [{}]，无法解析为 JSONObject 类型", o2.getClass().getName());
                return -1;
            }

            /**
             * 3. 根据表达式提取值，进行比较
             */
            JSONObject e1 = (JSONObject) o1;
            JSONObject e2 = (JSONObject) o2;
            // 判断 e1, e2 中是否有表达式指定的字段，如果某个字段为空，则进行排序
            String str1 = ExpressionParser.doHandleFieldUnit(
                    e1, ExpressionMatchUnit.buildFieldUnit(expression));
            String str2 = ExpressionParser.doHandleFieldUnit(
                    e2, ExpressionMatchUnit.buildFieldUnit(expression));

            if (str1 == null) return 1;
            if (str2 == null) return -1;
            // 判断 str1, str2 是否全为数字？如果是，解析数字并比较；如果不是，则直接排序；
            if (!str1.matches(FIELD_SORT_REGEX)) return 1;
            if (!str2.matches(FIELD_SORT_REGEX)) return -1;
            Integer v1 = Integer.parseInt(str1);
            Integer v2 = Integer.parseInt(str2);

            return (v1 > v2) ? 1 : (v1.equals(v2)) ? 0 : -1;
        };
    }
}
