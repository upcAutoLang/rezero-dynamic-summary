package com.grq.rezero.function.filter;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.grq.rezero.constants.FilterConstants;
import org.springframework.util.StringUtils;

/**
 * 对于传入的 List 的每个元素，获取每个元素的类，转换为 JSONObject，
 * 且每个JSONObject 转换结果的 Key 添加类型内码信息，即该类 simpleName 的小写；
 */
public class PrefixNameFilter implements NameFilter {
    static {
        ParserConfig.getGlobalInstance().setAsmEnable(false);
        SerializeConfig.getGlobalInstance().setAsmEnable(false);
    }

    /**
     * 构建 NameFilter 的 Key 值
     *
     * @param prefix 前缀
     * @param key 原 Key 值
     * @return
     */
    public static String buildFilterKey(String prefix, String key) {
        return prefix + FilterConstants.PRENAMEFILTER_SEPARATOR + key;
    }

    @Override
    public String process(Object o, String key, Object value) {
        Class clazz = o.getClass();
        String prefix = clazz.getSimpleName().toLowerCase();

        if (!StringUtils.isEmpty(key)) {
            return PrefixNameFilter.buildFilterKey(prefix, key);
        }
        return key;
    }
}
