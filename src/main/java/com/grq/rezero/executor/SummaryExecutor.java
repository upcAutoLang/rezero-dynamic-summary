package com.grq.rezero.executor;

import com.alibaba.fastjson.JSONArray;
import com.grq.rezero.dimension.units.AbstractDimensionChainUnit;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.summary.SummaryUnit;
import com.grq.rezero.summary.SummaryUnitChain;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 统计执行器
 */
@Component("summaryExecutor")
public class SummaryExecutor {
    private static Logger LOG = LoggerFactory.getLogger(SummaryExecutor.class);

    @Autowired
    private DimenStreamExecutor dimenStreamExecutor;

    public String summary(JSONArray src, SummaryUnitChain summaryUnitChain) {
        StringBuilder builder = new StringBuilder();
        List<SummaryUnit> summaryUnits = summaryUnitChain.getSummaryUnits();

        if (CollectionUtils.isEmpty(summaryUnits)) {
            LOG.warn("当前统计单元链为空，直接返回空字符串");
            return "";
        }
        for (SummaryUnit summaryUnit : summaryUnits) {
            List<AbstractDimensionChainUnit> dimenUnits = summaryUnit.getUnits();
            if (!CollectionUtils.isEmpty(dimenUnits)) {
                try {
                    String processResult = dimenStreamExecutor.doProcessDimenChainUnits(src, dimenUnits);
                    builder.append(processResult);
                } catch (ClassCastException | DynamicSummaryException e) {
                    LOG.error(e.getMessage());
                    return builder.toString();
                }
            }
        }
        return builder.toString();
    }
}
