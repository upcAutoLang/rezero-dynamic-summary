package com.grq.rezero.function.calculate;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.grq.rezero.constants.FunctionNameConstants;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * 通过表达式计算百分比
 */
public class PercentFunction extends AbstractFunction implements FunctionNameConstants {
    @Override
    public String getName() {
        return FUNCTION_NAME_COMMON_PERCENT;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Number leftNum = FunctionUtils.getNumberValue(arg1, env);
        Number rightNum = FunctionUtils.getNumberValue(arg2, env);

        Double left = leftNum.doubleValue();
        Double right = rightNum.doubleValue();

        DecimalFormat df = new DecimalFormat("###.00");
        Double percent = (left / right > 1 ? 1 : left / right) * 100;
        return new AviatorString(df.format(percent) + "%");
    }
}
