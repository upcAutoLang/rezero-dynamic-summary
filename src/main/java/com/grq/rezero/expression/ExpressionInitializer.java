package com.grq.rezero.expression;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.grq.rezero.function.FunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表达式处理执行者
 */
@Component("expressionInitializer")
@PropertySource("classpath:properties/function/function-info-properties")
public class ExpressionInitializer implements InitializingBean {
    private Logger LOG = LoggerFactory.getLogger(ExpressionInitializer.class);

    @Value("${AVIATOR_CUSTOM_FUNCTION_LIST}")
    private String allFunctions;

    @Autowired
    private FunctionFactory functionFactory;

    /**
     * 获取 Aviator 方法
     * <pre>
     *     Key 为 FunctionNameConstants 中定义的方法名称；
     *     Value 为自定义 Aviator 方法；
     * </pre>
     */
    private Map<String, AbstractFunction> aviatorFunctionMap = new ConcurrentHashMap<>();

    /**
     * 向 AviatorEvaluator 中添加所有自定义方法
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.isEmpty(allFunctions)) {
            List<String> functionList = Arrays.asList(allFunctions.split(","));
            for (String functionName : functionList) {
                AbstractFunction function = functionFactory.buildFunction(functionName);
                aviatorFunctionMap.put(functionName, function);
                // 在 AviatorEvaluator 中添加自定义方法
                try {
                    if (null != function) {
                        AviatorEvaluator.addFunction(function);
                        LOG.info("AviatorEvaluator 已添加 [{}] 自定义方法", functionName);
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("ExpressionInitializer 初始化添加 AviatorEvaluator 方法错误，错误 Aviator 方法名为 [{}]", functionName);
                    LOG.error(errorMsg);
                }
            }
        }
    }

    /**
     * 获取 Aviator 方法集合
     * @return
     */
    public Map<String, AbstractFunction> getAviatorFunctionMap() {
        return aviatorFunctionMap;
    }
}
