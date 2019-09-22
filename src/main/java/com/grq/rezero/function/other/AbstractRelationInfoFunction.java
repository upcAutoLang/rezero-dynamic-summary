package com.grq.rezero.function.other;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;

/**
 * 关联信息表达式，具体实现从由开发者实现
 */
public abstract class AbstractRelationInfoFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    @Override
    public String getName() {
        return FUNCTION_NAME_LISTADVANCED_RELATIONINFO;
    }
}
