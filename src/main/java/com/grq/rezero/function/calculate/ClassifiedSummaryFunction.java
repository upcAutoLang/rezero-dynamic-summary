package com.grq.rezero.function.calculate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.WrongMatchTypeException;
import com.grq.rezero.expression.ExpressionInfo;
import com.grq.rezero.expression.ExpressionMatchType;
import com.grq.rezero.expression.ExpressionMatchUnit;
import com.grq.rezero.expression.ExpressionParser;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     统计方法，用于将已分类的列表分别统计；
 *
 *     输入：
 *         1. entity - JSONObject (String, JSONArray)，已分类的列表；
 *             - Key: 分类字段值；
 *             - Value: 根据分类字段值进行分类的列表；
 *         2. expression - String，输出结果表达式，内部支持 #{...}#, ${...}$ 提取表达式，被提取的表达式在方法中被统计；
 *     输出：JSONArray (String) - 根据表达式计算得到的结果；
 * </pre>
 */
public class ClassifiedSummaryFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    /**
     * src (JSONObject (String, JSONArray)) - 传入 JSONObject
     */
    public static String SUMCLASSIFIED_ENTITY = FUNCTION_VAR_COMMON_ENTITY;
    /**
     * expression (String) - 表达式
     */
    public static String SUMCLASSIFIED_AVIATROARGS = FUNCTION_VAR_COMMON_AVIATORARGS;
    /**
     * 传入表达式中，通常只支持表达式计算，且需要类型的方法；除了这些方法之外，只有分类结果的 Key 值可以被传入；
     *
     * 在表达式中存在 SUMCLASSIFIED_KEY_MOCK 字段，该字段会被替换为当前分类的 Key 值；
     */
    public static String SUMCLASSIFIED_KEY_MOCK = "_%KEY%_";
    private Logger LOG = LoggerFactory.getLogger(ClassifiedSummaryFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_ENTITY2LIST_CLASSIFIED_SUMMARY;
    }

    /**
     * <pre>
     *     对分类结果进行统计：
     *
     *     分类逻辑如下：
     *     1. 获取 Aviator 参数；
     *     2. 判断传入的 expression 信息，如果参数数量为空，则抛出异常；
     *     3. 提取表达式中所有的 #{...}#，与传入的待提取字段数量进行比较；比较结果失败，则抛出异常；
     *     4. 修改传入的表达式，并给予修改后基础上填入 env 信息；
     *     5. 执行统计
     *          (1) 遍历所有分类标签，对标签下的分类结果 (JSONArray) 作统计的后续准备；
     *          (2) 替换模板中的分类标签信息；
     *          (3) 根据传入的 AviatorArgs 与 AviatorValues，进行统计；
     *
     *     注：
     *     1. 方法传入参数 args2 类型为 String[]，且内容如下：
     *     (1) index[0]: 表达式；
     *     (2) index[1...]: 待匹配的传入参数；
     *     表达式 index[0] 中包含若干 #{...}# 格式的字符串，且数量必须与 index[i...] 的个数相同，否则抛出异常；
     * </pre>
     *
     * @param env  包含 list (JSONArray), aviatorArgs (String)
     * @param arg1 list (JSONArray) - 输入 JSONArray，通常该值需要在之前进行分类；
     * @param arg2 aviatorArgs (String[]) - 传参数组
     *             - index[0]；表达式
     *             - index[1...]: 待匹配的传入参数；
     * @return
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONObject entity = null;
        Object expressionObject = null;
        String[] expressions = null;

        /**
         * 1. 获取 Aviator 参数；
         */
        try {
            entity = (JSONObject) FunctionUtils.getJavaObject(arg1, env);
            expressionObject = FunctionUtils.getJavaObject(arg2, env);
            expressions = (String[]) expressionObject;
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        /**
         * 2. 判断传入的 expressions 信息，如果参数数量为空，则抛出异常；
         */
        if (expressions == null) {
            String errorMsg = "ClassifiedSummaryFunction 未传入参数";
            throw new DynamicSummaryException(errorMsg);
        }

        /**
         * 3. 提取表达式所有的 #{...}#，与传入的待提取字段数量进行比较；比较结果失败，则抛出异常；
         *
         * (1) arg2 数组中第一个值为表达式，记在表达式中 #{...}# 的数量为 num1；
         * (2) arg2 数组中第二个之后的所有内容为待提取男子短，数量为 num2；
         * (3) 如果 num1 != num2，则抛出异常；
         */
        String expressionModel = (expressions[0] == null) ? "" : expressions[0];
        // 匹配 Aviator 方法个数
        int aviatorMatchCount = ExpressionParser.matchRegexCount(expressionModel, ExpressionParser.AVIATOR_REGEX_PATTERN);
        // 除了第一个表达式之外，传入其他的 Aviator 参数个数
        int aviatorValuesCount = expressions.length - 1;
        // 判断两个数量不匹配，抛出错误
        if (aviatorMatchCount != aviatorValuesCount) {
            String argsString = (expressions.length > 1)
                    ? StringUtils.join(Arrays.copyOfRange(expressions, 1, expressions.length - 1)) : "";
            String errorMsg = String.format("Aviator 方法个数 (%d) 与传入参数个数 (%d) 数量不匹配，抛出错误，具体信息：传入表达式为 [%s]，Aviator 具体参数为 [%s]",
                    aviatorMatchCount, aviatorValuesCount, expressionModel, argsString);
            throw new DynamicSummaryException(errorMsg);
        }

        /**
         * 4. 修改传入的表达式，并基于修改后基础上填入 env 信息；
         */
        String[] aviatorValues = Arrays.copyOfRange(expressions, 1, expressions.length);
        String[] aviatorArgs = new String[aviatorValues.length];

        // 正则表达式正向匹配
        String aviatorArgRegex = "aviatorArgs(?=[^_])";
        String baseString = "aviatorArgs";
        for (int i = 0; i < aviatorValues.length; i++) {
            String arg = baseString + "_" + i;
            expressionModel = expressionModel.replaceFirst(aviatorArgRegex, arg);
            aviatorArgs[i] = arg;
        }

        /**
         * 5. 执行统计
         * (1) 遍历所有分类标签，对标签下的分类结果 (JSONArray) 作统计的后续准备；
         * (2) 替换模板中的分类标签信息；
         * (3) 根据传入的 AviatorArgs 与 AviatorValues，进行统计；
         */
        JSONArray result = new JSONArray();
        if (!MapUtils.isEmpty(entity)) {
            // entity 的 keySet 为分类标签，遍历标签对应的分类结果 (JSONArray)，根据表达式进行计算；
            for (String key : entity.keySet()) {
                JSONArray classifiedList;
                try {
                    classifiedList = entity.getJSONArray(key);
                } catch (ClassCastException e) {
                    Object o = entity.get(key);
                    String className = o.getClass().getName();
                    String errorMsg = String.format("格式转换错误：分类结果应为 JSONArray 类型，当前类型为 [%s]，无法转换", className);
                    LOG.error(errorMsg);
                    throw new DynamicSummaryException(errorMsg, e);
                }

                // 表达式输出结果
                String expressionOutput = expressionModel;
                // 如果表达式中需要 Key 值内容（即表达式中包含 SUMCLASSIFIED_KEY_MOCK 字段），则替换 Key 值
                expressionOutput = expressionOutput.replaceAll(SUMCLASSIFIED_KEY_MOCK, key);

                /**
                 * 汇总方法进行处理
                 */
                // 表达式解析
                ExpressionInfo expressionInfo = ExpressionParser.parserExpression(expressionOutput);
                List<ExpressionMatchUnit> matchUnits = expressionInfo.getMatchUnits();

                // 获取表达式信息，作为处理结果的基础字符串；
                String handleResult = expressionOutput;
                // 遍历所有匹配单元信息，从 entity 中获取具体值，将表达式内容替换
                for (int i = matchUnits.size() - 1; i >= 0; i--) {
                    ExpressionMatchUnit matchUnit = matchUnits.get(i);
                    ExpressionMatchType type = matchUnit.getMatchType();
                    // 根据单元的不同类型分别进行处理，将处理结果逐步替换表达式内容
                    String expResult = null;
                    if (type.equals(ExpressionMatchType.FUNCTION)) {
                        expResult = ExpressionParser.doSummaryFunctionUnit(classifiedList, matchUnit, aviatorArgs, aviatorValues);
                    } else {
                        String errorMsg = "匹配单元类型错误：必须为 FUNCTION 类型";
                        LOG.error(errorMsg);
                        throw new WrongMatchTypeException(errorMsg);
                    }
                    // 结果替换
                    handleResult = handleResult.replace(matchUnit.getContent(), expResult);
                }
                result.add(handleResult);
            }
        }

        return new AviatorRuntimeJavaType(result);
    }
}
