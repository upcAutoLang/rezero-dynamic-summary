package com.grq.rezero.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.grq.rezero.dimension.units.AbstractDimensionChainUnit;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.expression.ExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 维度单元执行链
 */
@Component("dimenStreamExecutor")
public class DimenStreamExecutor<T extends JSON> {
    private Logger LOG = LoggerFactory.getLogger(DimenStreamExecutor.class);

    @Autowired
    private ExpressionParser initializer;

    /**
     * <pre>
     *     维度单元顺序执行：
     *     传入单挑数据 (JSONObject) 与维度单元列表，逐步执行，最后输出 String；
     *
     *     1. 传入的维度单元列表，要求按照顺序匹配输入输出类型；
     *     - 例如：需要<strong>第一个的输出类型是第二个的输入类型</strong>，第二个的输出类型是第三个的输入类型...
     *     - 如果单元 [n] 的输出与单元 [n+1] 的输入没有匹配上，则抛出异常；
     *     2. 方法的传入参数类型被规定为 JSONObject，输出类型被规定为 String；只要满足最初输入与最终输出类型，且处理不出错误，维度单元链就可以执行；
     * </pre>
     *
     * @param entity     单条数据
     * @param dimenUnits 维度单元列表
     * @return 输出结果为字符串
     * @throws ClassCastException      处理过程中抛出的转换类型错误
     * @throws DynamicSummaryException 动态统计过程中抛出的异常
     */
    public String doProcessDimenChainUnits(T entity, List<AbstractDimensionChainUnit> dimenUnits)
            throws ClassCastException, DynamicSummaryException {
        /**
         * 1. 判断传入的 entity 类型
         */
        if (!(entity instanceof JSONObject || entity instanceof JSONArray)) {
            String className = (entity == null)
                    ? "NULL" : entity.getClass().getName();
            String errorMsg = String.format("传入类型为 [%s]，不能转换为 JSON 类型", className);
            throw new ClassCastException(errorMsg);
        }

        /**
         * 2. 判断传入的维度单元列表类型是否匹配
         */
        if (!checkDimenUnits(dimenUnits)) {
            String errorMsg = "维度单元列表类型不匹配";
            throw new ClassCastException(errorMsg);
        }

        Object result = entity;
        int i = 0;
        AbstractDimensionChainUnit unit = null;

        for (i = 0; i < dimenUnits.size(); i++) {
            unit = dimenUnits.get(i);
            try {
                result = unit.doDimensionExec(result);
            } catch (ClassCastException e) {
                String errorMsg = String.format("第 %d 个维度单元 [%s] (从 0 计) 类型转换错误，具体信息：[%s]", i, JSON.toJSONString(unit));
                throw new DynamicSummaryException(errorMsg, e);
            } catch (DynamicSummaryException e) {
                String errorMsg = String.format("第 %d 个维度单元 [%s] (从 0 计) 抛出异常，具体信息：[%s]", i, JSON.toJSONString(unit));
                throw new DynamicSummaryException(errorMsg, e);
            }
        }

        if (result != null && !(result instanceof List)) {
            return result.toString();
        } else {
            throw new DynamicSummaryException("最终结果不是 String，维度转换结果错误");
        }
    }

    private boolean checkDimenUnits(List<AbstractDimensionChainUnit> units) {
        return true;
    }
}
