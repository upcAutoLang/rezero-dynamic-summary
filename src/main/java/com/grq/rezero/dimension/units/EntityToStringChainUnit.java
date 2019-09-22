package com.grq.rezero.dimension.units;

import com.alibaba.fastjson.JSONObject;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;

public class EntityToStringChainUnit extends AbstractDimensionChainUnit<JSONObject, String>
        implements FunctionVariableConstants {
    public EntityToStringChainUnit() {
    }

    public EntityToStringChainUnit(String expression, String[] aviatorArgs) {
        super(expression, aviatorArgs);
    }

    @Override
    public String doDimensionExec(JSONObject src) throws DynamicSummaryException {
        return null;
    }
}
