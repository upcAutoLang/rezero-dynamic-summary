package com.grq.rezero.summary;

import com.grq.rezero.dimension.units.AbstractDimensionChainUnit;

import java.util.Arrays;
import java.util.List;

/**
 * 统计单元
 */
public class SummaryUnit {
    private List<AbstractDimensionChainUnit> units;

    public SummaryUnit() {
    }

    public SummaryUnit(List<AbstractDimensionChainUnit> units) {
        this.units = units;
    }

    public SummaryUnit(AbstractDimensionChainUnit... units) {
        this.units = Arrays.asList(units);
    }

    public List<AbstractDimensionChainUnit> getUnits() {
        return units;
    }

    public void setUnits(List<AbstractDimensionChainUnit> units) {
        this.units = units;
    }
}
