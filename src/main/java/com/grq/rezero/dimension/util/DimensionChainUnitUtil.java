package com.grq.rezero.dimension.util;

import com.grq.rezero.dimension.units.AbstractDimensionChainUnit;
import com.grq.rezero.dimension.units.OutputStringChainUnit;

import java.util.Collections;
import java.util.List;

/**
 * 维度链表单元工具类
 */
public class DimensionChainUnitUtil {
    public static List<AbstractDimensionChainUnit> buildChainUnits(String string) {
        AbstractDimensionChainUnit stringUnit = new OutputStringChainUnit(null, string);
        return Collections.singletonList(stringUnit);
    }
}
