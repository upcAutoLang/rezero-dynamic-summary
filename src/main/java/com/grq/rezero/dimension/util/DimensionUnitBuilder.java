package com.grq.rezero.dimension.util;

import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.dimension.units.*;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.function.FunctionType;
import com.grq.rezero.function.calculate.ClassifiedSummaryFunction;
import com.grq.rezero.function.calculate.JoinFunction;
import com.grq.rezero.function.calculate.SumByFieldFunction;
import com.grq.rezero.function.filter.ClassifyFilterFunction;
import com.grq.rezero.function.filter.FieldEqualFilterFunction;
import com.grq.rezero.function.other.ListExtractFunction;
import com.grq.rezero.function.other.RelationInfoFunction;
import com.grq.rezero.function.other.SubListFunction;
import com.grq.rezero.function.sort.FieldSortFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 根据传入构建维度单元
 */
@Component("dimensionUnitBuilder")
public class DimensionUnitBuilder {
    private static Logger LOG = LoggerFactory.getLogger(DimensionUnitBuilder.class);

    public AbstractDimensionChainUnit buildDimenUnit(FunctionType type, String... args)
            throws DynamicSummaryException {
        if (type == null) {
            String errorMsg = "传入方法类型信息 FunctionType 为空，直接返回";
            LOG.error(errorMsg);
            throw new DynamicSummaryException(errorMsg);
        }
        // 根据方法类型信息，获取对应的表达式
        String expression = this.buildDimenUnitExpression(type);
        /**
         * 根据方法类型信息，构建对应的维度单元
         */
        AbstractDimensionChainUnit unit = null;
        switch (type) {
            case ENTITY2STRING_COMMON:
                unit = new CommonChainUnit(expression, args);
                break;
            /**
             * 二维 → 零维：输入 JSONArray，输出 String
             */
            case LIST2STRING_CLASSIFIED_SIZE:
            case LIST2STRING_JOIN:
            case LIST2STRING_SUMMARYBYFIELD:
                unit = new ListToStringChainUnit(expression, args);
                break;
            /**
             * 二维 → 一维：输入 JSONArray，输出 JSONObject
             */
            case LIST2ENTITY_CLASSIFY:
                unit = new ListToEntityChainUnit(expression, args);
                break;
            /**
             * 二维增强方法：输入 JSONArray，输出 JSONArray
             */
            case LISTADVANCED_FIELDEQUAL:
            case LISTADVANCED_EXTRACTOR:
            case LISTADVANCED_FIELDSORT:
            case LISTADVANCED_RELATIONINFO:
                unit = new ListAdvancedChainUnit(expression, args);
                break;
            /**
             * 一维增强方法：输入 JSONObject，输出 JSONObject
             */
            case ENTITYADVANCED_RELATIONINFO:
                unit = new EntityAdvancedChainUnit(expression, args);
                break;
            /**
             * 一维 → 二维：输入 JSONObject，输出 JSONArray
             */
            case ENTITY2LIST_CLASSIFIED_SUMMARY:
            case ENTITY2LIST_SUBLIST:
                unit = new EntityToListChainUnit(expression, args);
                break;
            default:
        }
        return unit;
    }

    /**
     * 传入方法类型，构建维度单元
     *
     * @param type 方法类型
     * @return
     */
    public String buildDimenUnitExpression(FunctionType type) {
        StringBuilder builder = new StringBuilder();
        switch (type) {
            case ENTITY2STRING_COMMON:
                builder.append("");
                break;
            case LIST2STRING_SUMMARYBYFIELD:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LIST2STRING_SUMMARYBYFIELD).append("(");
                builder.append(SumByFieldFunction.SUMBYFIELD_LIST).append(",");
                builder.append(SumByFieldFunction.SUMBYFIELD_AVIATORARGS).append(")");
                break;
            case LIST2STRING_CLASSIFIED_SIZE:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LIST2STRING_CLASSIFIED_SIZE).append("(");
                builder.append(ClassifyFilterFunction.CLASSIFY_LIST).append(",");
                builder.append(ClassifyFilterFunction.CLASSIFY_AVIATORARGS).append(")");
                break;
            case LIST2STRING_JOIN:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LIST2STRING_JOIN).append("(");
                builder.append(JoinFunction.JOINFUNCIONT_LIST).append(",");
                builder.append(JoinFunction.JOINFUNCTION_ARVIATORARGS).append(")");
                break;
            /**
             * 二维 → 一维：输入 JSONArray，输出 JSONObject
             */
            case LIST2ENTITY_CLASSIFY:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LIST2ENTITY_CLASSIFY).append("(");
                builder.append(ClassifyFilterFunction.CLASSIFY_LIST).append(",");
                builder.append(ClassifyFilterFunction.CLASSIFY_AVIATORARGS).append(")");
                break;
            /**
             * 二维增强方法：输入 JSONArray，输出 JSONArray
             */
            case LISTADVANCED_FIELDEQUAL:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LISTADVANCED_FIELDEQUAL).append("(");
                builder.append(FieldEqualFilterFunction.FIELDEQUAL_LIST).append(",");
                builder.append(FieldEqualFilterFunction.FIELDEQUAL_AVIATORARGS).append(")");
                break;
            case LISTADVANCED_EXTRACTOR:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LISTADVANCED_EXTRACTOR).append("(");
                builder.append(ListExtractFunction.LISTEXTRACT_LIST).append(",");
                builder.append(ListExtractFunction.LISTEXTRACT_AVIATORARGS).append(")");
                break;
            case LISTADVANCED_FIELDSORT:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LISTADVANCED_FIELDSORT).append("(");
                builder.append(FieldSortFunction.FIELDSORT_LIST).append(",");
                builder.append(FieldSortFunction.FIELDSORT_AVIATORARGS).append(")");
                break;
            case LISTADVANCED_RELATIONINFO:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LISTADVANCED_RELATIONINFO).append("(");
                builder.append(RelationInfoFunction.FUNCTION_VAR_COMMON_LIST).append(",");
                builder.append(RelationInfoFunction.FUNCTION_VAR_COMMON_AVIATORARGS).append(")");
                break;
            /**
             * 一维增强方法：输入 JSONObject，输出 JSONObject
             */
            case ENTITYADVANCED_RELATIONINFO:
                builder.append(FunctionNameConstants.FUNCTION_NAME_LISTADVANCED_RELATIONINFO).append("(");
                builder.append(RelationInfoFunction.FUNCTION_VAR_COMMON_ENTITY).append(",");
                builder.append(RelationInfoFunction.FUNCTION_VAR_COMMON_AVIATORARGS).append(")");
                break;
            /**
             * 一维 → 二维：输入 JSONObject，输出 JSONArray
             */
            case ENTITY2LIST_CLASSIFIED_SUMMARY:
                builder.append(FunctionNameConstants.FUNCTION_NAME_ENTITY2LIST_CLASSIFIED_SUMMARY).append("(");
                builder.append(ClassifiedSummaryFunction.SUMCLASSIFIED_ENTITY).append(",");
                builder.append(ClassifiedSummaryFunction.SUMCLASSIFIED_AVIATROARGS).append(")");
                break;
            case ENTITY2LIST_SUBLIST:
                builder.append(FunctionNameConstants.FUNCTION_NAME_ENTITY2LIST_SUBLIST).append("(");
                builder.append(SubListFunction.SUBLIST_ENTITY).append(",");
                builder.append(SubListFunction.SUBLIST_AVIATORARGS).append(")");
                break;
            default:
                String errorMsg = String.format("不存在类型为 [%s] 的维度单元构建方法，待开发人员添加", type.toString());
                LOG.error(errorMsg);
                throw new DynamicSummaryException(errorMsg);
        }
        return builder.toString();
    }
}
