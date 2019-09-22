package com.grq.rezero.function.calculate;

import com.alibaba.fastjson.JSONArray;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 字符串合并表达式，将 JSONArray 中指定字段的值提取为 List(String)，并用指定分割符将列表连结
 */
public class JoinFunction extends AbstractFunction
        implements FunctionNameConstants, FunctionVariableConstants {
    public static final String JOINFUNCIONT_LIST = FUNCTION_VAR_COMMON_LIST;
    public static final String JOINFUNCTION_ARVIATORARGS = FUNCTION_VAR_COMMON_AVIATORARGS;
    // 默认 join 方法的分隔符
    public static final String JOIN_DEFAULT_SPLITTER = "，";
    private Logger LOG = LoggerFactory.getLogger(JoinFunction.class);

    @Override
    public String getName() {
        return null;
    }

    /**
     * 通过传入表达式，从 JSONArray 中提取指定字段的元素；
     *
     * @param env
     * @param arg1
     * @param arg2
     * @return
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        JSONArray list = null;
        String separator = null;
        try {
            list = (JSONArray) FunctionUtils.getJavaObject(arg1, env);
            separator = FunctionUtils.getStringValue(arg2, env);
        } catch (ClassCastException e) {
            LOG.error("格式转换错误：[{}]", e.getMessage());
            throw new DynamicSummaryException(e.getMessage());
        } catch (Exception e) {
            LOG.error("未处理错误：[{}]", e.getMessage());
            e.printStackTrace();
            throw new DynamicSummaryException(e.getMessage(), e);
        }

        String result = "";
        if (!CollectionUtils.isEmpty(list)) {
            List<String> joinList = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String) {
                    joinList.add((String) obj);
                } else if (obj instanceof Map) {
                    Map<String, Object> entity = (Map) obj;
                    String str = (entity.get(FUNCTION_VAR_DEFAULT_TARGET_FIELD) == null)
                            ? "" : (String) entity.get(FUNCTION_VAR_DEFAULT_TARGET_FIELD);
                    joinList.add(str);
                }
            }
            result = StringUtils.join(joinList, separator);
        }
        return new AviatorString(result);
    }
}
