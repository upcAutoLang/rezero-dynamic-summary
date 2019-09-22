package com.grq.rezero.function.filter;

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
import com.grq.rezero.function.other.RelationInfoFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * <pre>
 *     分类方法
 *
 *     输入：JSONArray 与分类字段信息；
 *     输出：JSONObject
 *       - key: String，分类字段；
 *       - value: JSONArray，分类结果；
 * </pre>
 */
@Component("classifyFilterFunction")
@DependsOn("relationInfoFunction")
public class ClassifyFilterFunction extends FilterFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    /**
     * 默认分类参数，提取结果为 null 时，分类结果放在该 Key 下；
     */
    public static String DEFAULT_KEY = "_NULL_";
    /**
     * 定义 Aviator 表达式的传参
     */
    public static String CLASSIFY_LIST = FUNCTION_VAR_COMMON_LIST;
    public static String CLASSIFY_AVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;

    @Autowired
    private RelationInfoFunction relationInfoFunction;

    private Logger LOG = LoggerFactory.getLogger(ClassifyFilterFunction.class);

    @Override
    public String getName() {
        return FUNCTION_NAME_LIST2ENTITY_CLASSIFY;
    }

    /**
     * 通过表达式，从 JSONArray 中根据传入字段进行分类
     *
     * @param env 包含 list (JSONArray), expression (String)
     * @param arg1 list (JSONArray) - 输入添加了类型内码后的 JSONArray
     * @param arg2 expression (String) - 分类条件，根据传入字符串解析
     * @return
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String classifyExpression = null;

        /**
         * 提取 env 内的 Aviator 参数
         */
        JSONObject result = new JSONObject(true);
        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            classifyExpression = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        /**
         * 进行分类操作
         */
        if (CollectionUtils.isEmpty(list)) {
            return new AviatorRuntimeJavaType(result);
        }

        // 缺省分类列表的内容，放在返回 JSONObject 的最后
        JSONArray defaultClassifiedList = new JSONArray();
        try {
            for (Object obj : list) {
                JSONObject entity = (JSONObject) obj;
                /**
                 * 1. 解析传入的表达式，即提取分类的 Key 值
                 * 如果解析结果为空，则使用默认的 Key 值；
                 */
                String resultKey = ExpressionParser.doHandleFieldUnit(
                        entity, ExpressionMatchUnit.buildFieldUnit(classifyExpression));
                if (StringUtils.isEmpty(resultKey)) {
                    defaultClassifiedList.add(entity);
                    continue;
                }
                /**
                 * 2. 进行分类
                 * (1) 分类结果中不存在该 key，则新建 JSONArray，并将当前数据存入；
                 * (2) 分类结果中存在该 key，则向其中存入当前数据；
                 */
                JSONArray classifiedList;
                if (!result.containsKey(resultKey)) {
                    classifiedList = new JSONArray();
                    classifiedList.add(entity);
                } else {
                    classifiedList = result.getJSONArray(resultKey);
                    classifiedList.add(entity);
                }
                result.put(resultKey, classifiedList);
            }
            /**
             * 3. 将缺省分类列表置于最后
             */
            if (!CollectionUtils.isEmpty(defaultClassifiedList)) {
                result.put(DEFAULT_KEY, defaultClassifiedList);
            }
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new AviatorRuntimeJavaType(result);
    }
}
