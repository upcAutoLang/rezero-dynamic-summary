package com.grq.rezero.function.filter;

import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;

/**
 * 关联信息筛选方法
 */
public class RelationFilterFunction extends FilterFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    @Override
    public String getName() {
        return FUNCTION_NAME_LISTADVANCED_RELATIONFILTER;
    }
}
