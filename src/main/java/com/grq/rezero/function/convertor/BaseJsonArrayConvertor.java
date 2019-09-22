package com.grq.rezero.function.convertor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.grq.rezero.function.filter.PrefixNameFilter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 将基本的 List 转为 JSONArray，且为每个 JSONObject 的 Key 添加类型内码信息（递归）
 *
 * 当前类型内码信息使用类的 SimpleName 并转换为小写的结果；
 */
public class BaseJsonArrayConvertor {
    // 对于传入 List 的每个元素，获取每个元素的类转换为 JSONObject，且每个 JSONObject 转换结果的 Key 添加类型内码信息；
    private static PrefixNameFilter nameFilter = new PrefixNameFilter();

    /**
     * 将基本的 List 转为 JSONArray，且为每个 JSONObject 的 Key 添加类型内码信息（递归）；
     * 当前类型内码信息使用类的 SimpleName 并转换为小写的结果；
     * 例如对于 Student 类，则该类转为 JSONObject 后，每个字段前面都会加上 "student." 的前缀；
     *
     * @param list 原始列表
     * @param clazz 列表元素的 Class
     * @param <T> 泛型
     * @return 添加类型内码信息后的 JSONArray
     */
    public static <T> JSONArray convertToBaseJsonArray(List<T> list, Class<T> clazz) {
        // 方法初始判空
        if (CollectionUtils.isEmpty(list)) {
            return new JSONArray();
        }

        JSONArray result = new JSONArray();
        try {
            for (T element : list) {
                String elementJsonString = JSON.toJSONString(element, nameFilter);
                JSONObject entity = (JSONObject) JSON.parse(elementJsonString);
                result.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
