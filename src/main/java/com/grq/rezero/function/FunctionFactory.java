package com.grq.rezero.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.exception.WrongMatchException;
import com.grq.rezero.function.calculate.*;
import com.grq.rezero.function.filter.ClassifyFilterFunction;
import com.grq.rezero.function.filter.FieldEqualFilterFunction;
import com.grq.rezero.function.filter.RelationFilterFunction;
import com.grq.rezero.function.other.ListExtractFunction;
import com.grq.rezero.function.other.RelationInfoFunction;
import com.grq.rezero.function.sort.FieldSortFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 函数工厂，在 Spring 启动过程被 ExpressionInitializer 调用，将自定义方法加入到 AviatorEvaluator 中
 *
 * @see com.grq.rezero.expression.ExpressionInitializer
 */
@Component("functionFactory")
public class FunctionFactory implements FunctionNameConstants {
    @Autowired
    private ClassifyFilterFunction classifyFilterFunction;

    /**
     * 传入方法名称，用工厂方法构造 Aviator 表达式方法对象
     *
     * @param functionName 方法名臣个，在 FunctionNameConstants 中定义
     * @return
     * @see FunctionNameConstants
     */
    public AbstractFunction buildFunction(String functionName) {
        if (StringUtils.isEmpty(functionName)) {
            throw new WrongMatchException("工厂方法构造 Abstract Function 失败，传入 functionName 为空值");
        }
        AbstractFunction function = null;
        // 根据传入的方法名称，构建 Aviator 表达式方法对象
        switch (functionName) {
            /**
             * 通用单元方法
             */
            case FUNCTION_NAME_COMMON_PERCENT:
                function = new PercentFunction();
                break;
            /**
             * 二维 → 零维：输入 JSONArray，输出 String
             */
            case FUNCTION_NAME_LIST2STRING_SUMMARYBYFIELD:
                function = new SumByFieldFunction();
                break;
            case FUNCTION_NAME_LIST2STRING_JOIN:
                function = new JoinFunction();
                break;
            case FUNCTION_NAME_LIST2STRING_CLASSIFIED_SIZE:
                function = new ClassifiedSizeFunction();
                break;
            /**
             * 二维 → 一维，输入 JSONArray，输出 JSONObject
             */
            case FUNCTION_NAME_LIST2ENTITY_CLASSIFY:
                function = classifyFilterFunction;
                break;
            /**
             * 二维增强方法
             */
            case FUNCTION_NAME_LISTADVANCED_FIELDSORT:
                function = new FieldSortFunction();
                break;
            case FUNCTION_NAME_LISTADVANCED_FIELDEQUAL:
                function = new FieldEqualFilterFunction();
                break;
            case FUNCTION_NAME_LISTADVANCED_RELATIONFILTER:
                function = new RelationFilterFunction();
                break;
            case FUNCTION_NAME_LISTADVANCED_EXTRACTOR:
                function = new ListExtractFunction();
                break;
            /**
             * 一维增强方法：输入 JSONObject，输出 JSONObject
             */
            case FUNCTION_NAME_ENTITYADVANCED_RELATIONINFO:
                function = new RelationInfoFunction();
                break;
            case FUNCTION_NAME_ENTITY2LIST_CLASSIFIED_SUMMARY:
                function = new ClassifiedSummaryFunction();
                break;
            default:
                String errorMsg = String.format("传入值错误：未找到名为 [%s] 的方法", functionName);
                throw new WrongMatchException(errorMsg);
        }
        return function;
    }
}
