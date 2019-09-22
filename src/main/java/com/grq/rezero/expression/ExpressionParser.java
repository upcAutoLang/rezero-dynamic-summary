package com.grq.rezero.expression;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.exception.ExpressionSyntaxErrorException;
import com.grq.rezero.constants.FunctionNameConstants;
import com.grq.rezero.constants.FunctionVariableConstants;
import com.grq.rezero.exception.DynamicSummaryException;
import com.grq.rezero.exception.EmptyException;
import com.grq.rezero.exception.WrongMatchException;
import com.grq.rezero.exception.WrongMatchTypeException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *     用于解析从前端传来的表达式，表达式为字符串形式，字符串中是【常量表达式 + Aviator 表达式】形式；
 *
 *     1. Aviator 表达式：
 *     (1) 使用 ${...}$ 形式，表示获取单条数据的某个字段值；
 *     匹配过程中，使用<strong>非贪婪匹配模式</strong>（表达式中的 .*? 决定使用非贪婪模式）；
 *
 *     (2) 使用 #{...}$ 形式，表示调用 Aviator 方法；
 *     匹配过程中，使用<strong>非贪婪匹配模式</strong>（表达式中的 .*? 决定使用非贪婪模式）；
 *     #{...}# 表示传入 Aviator 表达式，且 Aviator 本身已经<strong>支持方法的嵌套</strong>，所以在 #{...}# 表达式中不允许嵌套同样的表达式；
 *
 *     2. 常量表达式：
 *     在 Aviator 表达式之外的表示，全部用常量形式表达；
 * </pre>
 */
public class ExpressionParser {
    /**
     * 解析 ${...}$ 正则表达式，用于提取单条数据的某个字段值；
     *
     * <strong>${...}$ 内部使用非贪婪匹配模式；</strong>
     */
    public static final String FIELD_REGEX = "\\$\\{.*?}\\$";
    public static final Pattern FIELD_REGEX_PATTERN = Pattern.compile(FIELD_REGEX);

    /**
     * 解析 ${...}$ 正则表达式，用于提取 Aviator 表达式信息；
     *
     * <strong>${...}$ 内部使用非贪婪匹配模式；</strong>
     */
    public static final String FUNCTION_REGEX = "#\\{.*?}#";
    public static final Pattern FUNCTION_REGEX_PATTERN = Pattern.compile(FUNCTION_REGEX);

    /**
     * <pre>
     *     解析 Aviator 表达式，用于已经判断为符合 Aviator 表达式的方法，并通过正则表达式的 Group 获取具体信息；
     *
     *     - Group 0: Aviator 全部表达式；
     *     - Group 1: 方法名；
     *     - Group 2: 参数 1 (一般为 "entity" 或 "list")
     *     - Group 3: 参数 2 (一般为 "aviatorArgs)
     * </pre>
     */
    public static final String AVIATOR_REGEX = "(\\w+)\\(([a-zA-Z0-9]*),([a-zA-Z0-9]*)\\)";
    public static final Pattern AVIATOR_REGEX_PATTERN = Pattern.compile(AVIATOR_REGEX);

    /**
     * <pre>
     *     字段解析关联信息的正则表达式：
     *
     *     字段解析器对 ${...}$ 中的表达式信息进行解析，其中：
     *     1. 表达式以 '@' 为关联信息查询标识，'@' 前的信息用作解析战备执勤内码；
     *     2. 表达式在 '@' 后的信息用于获取指定的关联信息，且以 '.' 为分割符，逐层获取关联信息内容；
     *
     *     <strong>例：</strong>如果想获取学生关联出的<strong>大学的地点信息</strong>，则表达式为：
     *     <code>${student@college.location}</code>
     * </pre>
     */
    public static final String EXPRESSION_RELATIONINFO_REGEX_STRING = "([^@}{]*)@([^@}{]*)";
    public static final Pattern EXPRESSION_RELATIONINFO_REGEX_PATTERN = Pattern.compile(EXPRESSION_RELATIONINFO_REGEX_STRING);
    public static String EXPRESSION_RELATIONINFO_SPLITTER = "\\.";

    public static Logger LOG = LoggerFactory.getLogger(ExpressionParser.class);

    /**
     * 传入表达式，将表达式解析后的信息存入 ExpressionInfo 中
     *
     * @param expression 待解析表达式
     * @return 解析结果
     * @see ExpressionInfo
     */
    public static ExpressionInfo parserExpression(String expression) {
        // 解析字段
        List<ExpressionMatchUnit> functionExpressions = ExpressionParser.parseFunctionsFromExpression(expression);
        List<ExpressionMatchUnit> fieldsExpressions = ExpressionParser.parseFieldsFromExpression(expression);
        // 将两个结果合并
        List<ExpressionMatchUnit> expressionUnits = mergeFunctionAndFieldsUnits(functionExpressions, fieldsExpressions);

        // 表达式结果
        ExpressionInfo expressionInfo = new ExpressionInfo();
        expressionInfo.setExpression(expression);
        expressionInfo.setMatchUnits(expressionUnits);
        return expressionInfo;
    }

    /**
     * 将字段单元与字段单元筛选、合并、排序
     *
     * @param functions 方法单元列表
     * @param fields    字段单元列表
     * @return 合并后的结果
     * @throws WrongMatchTypeException 检查方法单元列表、字段单元列表，如果传入的两个列表没有被正确分类，则抛出异常；
     */
    public static List<ExpressionMatchUnit> mergeFunctionAndFieldsUnits(List<ExpressionMatchUnit> functions,
                                                                        List<ExpressionMatchUnit> fields) throws WrongMatchTypeException {
        return mergeFunctionAndFieldsUnits(functions, fields, true);
    }

    /**
     * 将字段单元与字段单元筛选、合并、排序
     * <pre>
     *     排序规则：
     *     1. <strong>方法匹配单元整体排在字段匹配单元之后；</strong>
     *      - 因为替换式采用<strong>倒序</strong>的替换规律，所以方法匹配被计算并替换；
     *      - 这样可以避免先替换字段匹配单元出现的问题：原表达式根据字段匹配单元进行替换，那么方法匹配单元的内容也可能会被替换，在计算方法中出现问题
     *     2. <strong>按照单元的起始位置从后往前排；</strong>
     *
     *     注：removeFieldInFunctions 参数：
     *     - {@code true} 进行筛选、合并、排序；
     *     例如有 fieldunit 的 [start,end]=[5,11]，又有 functionunit 的 [start,end]=[1,17]，则将 fieldunit 从列表中删除；
     *     - {@code false} 直接将两个传入参数 functions, fields 存入 ArrayList，不进行筛选、合并、排序；
     * </pre>
     *
     * @param functions              方法单元列表
     * @param fields                 字段单元列表
     * @param removeFieldInFunctions 是否将位置在方法单元列表中的字段单元删除？
     * @return 合并后的结果
     * @throws WrongMatchTypeException 检查方法单元列表、字段单元列表，如果传入的两个列表没有被正确分类，则抛出异常；
     */
    public static List<ExpressionMatchUnit> mergeFunctionAndFieldsUnits(List<ExpressionMatchUnit> functions,
                                                                        List<ExpressionMatchUnit> fields,
                                                                        boolean removeFieldInFunctions) throws WrongMatchTypeException {
        List<ExpressionMatchUnit> result = new ArrayList<>();
        // 不将方法单元中的 Field 单元删除，则直接累加即可；
        if (!removeFieldInFunctions) {
            result.addAll(functions);
            result.addAll(fields);
        } else {
            // 判断传入参数类型
            if (!checkFunctionUnits(functions)) {
                throw new WrongMatchTypeException("传入 functions 中有其他类型单元");
            }
            if (!checkFieldUnits(fields)) {
                throw new WrongMatchTypeException("传入 fields 中有其他类型单元");
            }

            /**
             * 将包含在方法匹配单元中的字段匹配单元剔除
             */
            result.addAll(functions);
            if (!CollectionUtils.isEmpty(fields)) {
                for (int i = fields.size() - 1; i >= 0; i--) {
                    ExpressionMatchUnit field = fields.get(i);
                    // 如果当前字段在方法单元列表中已经被包含，则将其删除
                    if (checkFieldInFunctionUnits(field, functions)) {
                        fields.remove(i);
                    }
                }
                // 将删除后的字段单元全部放入列表中
                result.addAll(fields);
            }
        }
        /**
         * 为结果进行排序；排序规则：
         * 1. <strong>方法匹配单元整体排在字段匹配单元之后；</strong>
         * - 因为替换式采用<strong>倒序</strong>的替换规律，所以方法匹配首先被计算并替换；
         * - 这样可以避免先替换字段匹配单元出现的问题：原表达式根据字段匹配单元进行替换，那么方法匹配单元的内容也可能会被替换，在计算方法中出现问题
         * 2. <strong>按照单元的起始位置从后往前排；</strong>
         */
        result.sort((o1, o2) -> {
            if (!o1.getMatchType().equals(o2.getMatchType())) {
                return ExpressionMatchType.FIELD.equals(o1.getMatchType())
                        ? -1 : 1;
            }
            // 先比较起始位置
            if (o1.getBegin() > o2.getBegin())
                return 1;
            else if (o1.getBegin() < o2.getBegin())
                return -1;

            if (o1.getEnd() < o2.getEnd())
                return 1;
            else if (o1.getEnd() > o2.getEnd())
                return -1;

            return 0;
        });
        return result;
    }

    /**
     * 检查传入的匹配单元是否全部为方法单元
     *
     * @param units
     * @return
     */
    public static boolean checkFunctionUnits(List<ExpressionMatchUnit> units) {
        if (!CollectionUtils.isEmpty(units)) {
            for (ExpressionMatchUnit unit : units) {
                if (!ExpressionMatchType.FUNCTION.equals(unit.getMatchType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查传入的匹配单元是否全部为字段单元
     *
     * @param units
     * @return
     */
    public static boolean checkFieldUnits(List<ExpressionMatchUnit> units) {
        if (!CollectionUtils.isEmpty(units)) {
            for (ExpressionMatchUnit unit : units) {
                if (!ExpressionMatchType.FIELD.equals(unit.getMatchType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查传入的字段单元是否在方法单元列表中
     *
     * @param field     待检查字段单元
     * @param functions 用于检查的方法单元
     * @return
     */
    public static boolean checkFieldInFunctionUnits(ExpressionMatchUnit field, List<ExpressionMatchUnit> functions) {
        if (!CollectionUtils.isEmpty(functions)) {
            for (ExpressionMatchUnit function : functions) {
                if (checkFieldInFunctionUnit(field, function))
                    return true;
            }
        }
        return false;
    }

    /**
     * 检查传入的字段单元是否在单个方法单元中
     *
     * @param field    待检查字段单元
     * @param function 用于检查的方法单元
     * @return
     */
    public static boolean checkFieldInFunctionUnit(ExpressionMatchUnit field, ExpressionMatchUnit function) {
        return field.getBegin() > function.getBegin()
                && field.getEnd() < function.getEnd();
    }

    /**
     * 传入表达式，通过解析 ${...}$ 正则表达式，将解析结果的括号内的内容用 List 返回
     *
     * @param expression 待解析表达式
     * @return
     */
    public static List<ExpressionMatchUnit> parseFieldsFromExpression(String expression) {
        List<ExpressionMatchUnit> result = new ArrayList<>();
        Matcher matcher = FIELD_REGEX_PATTERN.matcher(expression);
        while (matcher.find()) {
            String matchContent = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            ExpressionMatchUnit unit = new ExpressionMatchUnit(matchContent, ExpressionMatchType.FIELD, start, end);
            result.add(unit);
        }
        return result;
    }


    /**
     * 传入表达式，通过解析 #{...}# 正则表达式，将解析结果的括号内的内容用 List 返回
     *
     * @param expression 待解析表达式
     * @return
     */
    public static List<ExpressionMatchUnit> parseFunctionsFromExpression(String expression) {
        List<ExpressionMatchUnit> result = new ArrayList<>();
        Matcher matcher = FUNCTION_REGEX_PATTERN.matcher(expression);
        while (matcher.find()) {
            String matchContent = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            ExpressionMatchUnit unit = new ExpressionMatchUnit(matchContent, ExpressionMatchType.FUNCTION, start, end);
            result.add(unit);
        }
        return result;
    }

    /**
     * 传入单条数据，根据表达式 expression 处理并返回
     *
     * @param entity     单条数据
     * @param expression 处理表达式
     * @return 处理结果 (String)
     * @throws DynamicSummaryException 抛出处理过程中的错误异常
     */
    public static String executeExpression(JSONObject entity, String expression)
            throws DynamicSummaryException {
        // 表达式解析
        ExpressionInfo expressionInfo = ExpressionParser.parserExpression(expression);
        return doHandleExpression(entity, expressionInfo);
    }

    /**
     * 处理表达式
     * <pre>
     *     逐个匹配单元的处理表达式匹配单元；
     *
     *     1. 获取表达式信息，作为处理结果的基础字符串；
     *     2. 遍历匹配单元，根据单元的不同类型分别进行处理，将处理结果逐步替换表达式内容；
     *     3. 将表达式最后处理结果返回；
     * </pre>
     *
     * @param entity         单条数据内容
     * @param expressionInfo 表达式信息
     * @return 处理结果
     * @throws DynamicSummaryException 抛出处理过程中的错误异常
     */
    private static String doHandleExpression(JSONObject entity,
                                             ExpressionInfo expressionInfo) throws DynamicSummaryException {
        List<ExpressionMatchUnit> matchUnits = expressionInfo.getMatchUnits();

        // 获取表达式信息，作为处理结果的基础字符串；
        String handleResult = expressionInfo.getExpression();
        // 遍历所有匹配单元信息，从 entity 中获取具体值，将表达式内容替换
        for (int i = matchUnits.size() - 1; i >= 0; i--) {
            ExpressionMatchUnit matchUnit = matchUnits.get(i);
            ExpressionMatchType type = matchUnit.getMatchType();
            // 根据单元的不同类型分别进行处理，将处理结果逐步替换表达式内容
            String expResult = null;
            switch (type) {
                case FIELD:
                    expResult = doHandleFieldUnit(entity, matchUnit);
                    break;
                case FUNCTION:
                    expResult = doHandleFunctionUnit(entity, matchUnit);
                    break;
            }
            // 结果替换
            handleResult = handleResult.replace(matchUnit.getContent(), expResult);
        }
        return handleResult;
    }

    /**
     * 处理方法单元
     *
     * <pre>
     *     处理方法单元步骤：
     *     1. 判断方法类型：如果不为方法类型，抛出错误类型异常
     *     2. 执行方法
     * </pre>
     *
     * @param entity 单条数据信息
     * @param unit   字段单元
     * @return 处理字符串结果
     * @throws DynamicSummaryException 抛出匹配类型错误异常
     */
    public static String doHandleFunctionUnit(JSONObject entity, ExpressionMatchUnit unit) throws DynamicSummaryException {
        /**
         * 1. 判断方法类型：如果不为方法类型，抛出错误类型异常
         */
        ExpressionMatchType type = unit.getMatchType();
        if (!ExpressionMatchType.FUNCTION.equals(type)) {
            String unitJsonString = JSON.toJSONString(unit);
            String message = String.format("传入匹配单元类型不为【方法类型】，匹配单元详细信息：{%s}", unitJsonString);
            throw new WrongMatchTypeException(message);
        }

        /**
         * 2. 执行方法
         */
        return doExecuteFunction(entity, unit);
    }

    /**
     * 处理字段单元
     *
     * <pre>
     *     根据匹配单元内容，从数据 entity 中取值；
     * </pre>
     *
     * @param entity 单条数据信息
     * @param unit   字段单元
     * @return 从 entity 中获取结果字段的结果
     * @throws DynamicSummaryException 抛出匹配类型错误异常
     */
    public static String doHandleFieldUnit(JSONObject entity, ExpressionMatchUnit unit) throws DynamicSummaryException {
        /**
         * 1. 判断方法类型：如果不为字段类型，抛出错误类型异常
         */
        ExpressionMatchType type = unit.getMatchType();
        if (!ExpressionMatchType.FIELD.equals(type)) {
            String unitJsonString = JSON.toJSONString(unit);
            String message = String.format("传入匹配单元类型不为【字段类型】，匹配单元详细信息：{%s}", unitJsonString);
            throw new WrongMatchTypeException(message);
        }

        /**
         * 2. 如果是常规字段，直接替换
         */
        String e_k = unit.matchResult();
        Object e_v = entity.get(e_k);
        if (e_v != null) {
            return e_v.toString();
        }
        /**
         * 3. 如果没有从 JSONObject 中直接获取到值，则尝试使用关联字段的方法解析
         */
        String relationInfoResult = doHandleFieldRelationInfo(entity, unit);
        return (relationInfoResult == null) ? "" : relationInfoResult;
    }

    /**
     * 处理字段单元中的关联信息
     *
     * @param entity 单条数据信息
     * @param unit   字段单元
     * @return 从 entity 中获取关联信息的结果
     * @throws WrongMatchTypeException 抛出匹配类型错误异常
     */
    public static String doHandleFieldRelationInfo(JSONObject entity, ExpressionMatchUnit unit) throws WrongMatchTypeException {
        // 判断单元类型，如果不为字段单元，则抛出错误
        ExpressionMatchType type = unit.getMatchType();
        if (!ExpressionMatchType.FIELD.equals(type)) {
            String unitJsonString = JSON.toJSONString(unit);
            String message = String.format("传入匹配单元类型不为【字段类型】，匹配单元详细信息：{%s}", unitJsonString);
            throw new WrongMatchTypeException(message);
        }
        // 提取字段单元除了括号之外的信息
        String matchResult = unit.matchResult();
        Matcher matcher = EXPRESSION_RELATIONINFO_REGEX_PATTERN.matcher(matchResult);
        while (matcher.find()) {
            // 如果匹配结果 group != 2，即没有满足 k-v 格式，则跳过
            int groupCount = matcher.groupCount();
            if (groupCount == 2) {
                /**
                 * Group1 用于获取数据源名称，然后从 JSONObject 中获取所有关联信息
                 */
                String dataResource = matcher.group(1);
                String key = dataResource + "." + FunctionNameConstants.FUNCTION_NAME_ENTITYADVANCED_RELATIONINFO;
                JSONObject relationInfo = entity.getJSONObject(key);
                if (MapUtils.isEmpty(relationInfo)) {
                    LOG.debug("当前数据没有关联信息，匹配单元表达式为 [{}]", unit.getContent());
                    return "";
                }
                /**
                 * Group2 用于获取具体的关联信息
                 */
                String relationExpression = matcher.group(2);
                if (StringUtils.isEmpty(relationExpression)) {
                    LOG.debug("没有关联信息的内容，直接返回空字符串");
                    return "";
                }
                List<String> keys = Arrays.asList(relationExpression.split(EXPRESSION_RELATIONINFO_SPLITTER));
                JSONObject e = new JSONObject(relationInfo);
                String result = "";
                for (int i = 0; i < keys.size(); i++) {
                    String k = keys.get(i);
                    try {
                        if (i < keys.size() - 1) {
                            e = e.getJSONObject(k);
                        } else {
                            result = e.getString(k);
                        }
                    } catch (NullPointerException exp) {
                        String nullPointField = relationExpression.substring(0, relationExpression.indexOf(k) - 1);
                        String errorMsg = String.format("关联信息获取出现空字符串，错误信息：关联信息表达式为 [%s]，在获取字段 [%s] 时抛出空指针异常", relationExpression, nullPointField);
                        LOG.error(errorMsg);
                        throw new DynamicSummaryException(errorMsg, exp);
                    } catch (ClassCastException exp) {
                        Object o = e.get(k);
                        String errorMsg = String.format("类型转换错误，原类型为 [%s]，无法转换为 JSONObject 与 String 类型", o.getClass().getName());
                        LOG.error(errorMsg);
                        throw new DynamicSummaryException(errorMsg, exp);
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }
                return (result == null) ? "" : result;
            }
        }

        return "";
    }

    /**
     * 根据方法匹配单元与传入的 Aviator 参数数组与值数组，对列表进行统计：只支持统计方法
     *
     * @param list          数据列表，JSONArray 类型
     * @param unit          方法匹配单元
     * @param aviatorArgs   Aviator 表达式参数数组
     * @param aviatorValues Aviator 表达式数值数组
     * @return
     */
    public static String doSummaryFunctionUnit(JSONArray list, ExpressionMatchUnit unit, String[] aviatorArgs, String[] aviatorValues) {
        /**
         * 判断单元类型，如果不为方法单元，则抛出错误
         */
        ExpressionMatchType type = unit.getMatchType();
        if (!ExpressionMatchType.FUNCTION.equals(type)) {
            String unitJsonString = JSON.toJSONString(unit);
            String message = String.format("传入匹配单元类型不为【方法类型】，匹配单元详细信息：{%s}", unitJsonString);
            throw new WrongMatchTypeException(message);
        }

        /**
         * 判断传入的 aviatorArgs 与 aviatorValues 数量是否相同？如果不相同则抛出错误
         */
        if (aviatorArgs == null && aviatorValues == null) {
            LOG.warn("doSummaryFunctionUnit 传入的 AviatorArgs 与 AviatorValues 为空，统计结果直接返回空字符串");
            return "";
        }
        if (aviatorArgs == null && aviatorValues != null) {
            throw new DynamicSummaryException("doSummaryFunctionUnit 方法错误：传入 aviatorArgs 为空");
        }
        if (aviatorArgs != null && aviatorValues == null) {
            throw new DynamicSummaryException("doSummaryFunctionUnit 方法错误：传入 aviatorValues 为空");
        }
        if (aviatorArgs.length != aviatorValues.length) {
            String errorMsg = String.format("doSummaryFunctionUnit 方法错误：传入 AviatorArgs 数量 [%d] 个，aviatorValues 数量 [%d] 个，数量不匹配", aviatorArgs.length, aviatorValues.length);
            throw new WrongMatchException(errorMsg);
        }

        String functionExpression = unit.matchResult();
        Map<String, Object> env = new HashMap<>();
        env.put(FunctionVariableConstants.FUNCTION_VAR_COMMON_LIST, list);
        for (int i = 0; i < aviatorArgs.length; i++) {
            env.put(aviatorArgs[i], aviatorValues[i]);
        }
        Object result = AviatorEvaluator.execute(functionExpression, env);
        return result.toString();
    }

    /**
     * 执行 Aviator 表达式方法
     * <pre>
     *     1. 替换所有字段信息 (${...}$)
     *      - 获取方法字段的表达式内容，以表达式内容为基础，获取方法字段内的所有字段信息；
     *      - 如果有待替换字段，则<strong>更心烦昂发匹配单元字段信息</strong>；
     *     2. 抽取表达式内容，只匹配第一个结果；
     *     3. 执行 Aviator 表达式，并返回；
     * </pre>
     *
     * @param entity 单条数据信息
     * @param unit   匹配单元
     * @return
     */
    public static String doExecuteFunction(JSONObject entity, ExpressionMatchUnit unit) {
        /**
         * 1. 替换所有字段信息 (${...}$)
         *  - 获取方法字段的表达式内容，以表达式内容为基础，获取方法字段内的所有字段信息；
         *  - 如果有待替换字段，则<strong>更心烦昂发匹配单元字段信息</strong>；
         */
        String expression = unit.getContent();
        List<ExpressionMatchUnit> fieldUnits = parseFieldsFromExpression(expression);

        String replaced = expression;
        if (!CollectionUtils.isEmpty(fieldUnits)) {
            for (int i = fieldUnits.size() - 1; i >= 0; i--) {
                ExpressionMatchUnit fieldUnit = fieldUnits.get(i);
                String value = doHandleFieldUnit(entity, fieldUnit);
                // 替换结果
                replaced = replaced.replace(fieldUnit.getContent(), value);
            }
        }

        /**
         * 2. 抽取表达式内容，只匹配第一个结果
         */
        Matcher matcher = FUNCTION_REGEX_PATTERN.matcher(replaced);
        String aviatorExpression = "";
        while (matcher.find()) {
            aviatorExpression = matcher.group();
            aviatorExpression = aviatorExpression.substring(2, aviatorExpression.length() - 2);
            break;
        }

        /**
         * 3. 执行 Aviator 表达式，并返回
         */
        if (StringUtils.isEmpty(aviatorExpression)) {
            throw new EmptyException("doExecuteFunction 执行 Aviator 表达式错误，表达式为空");
        }
        Object result = null;
        try {
            result = AviatorEvaluator.execute(aviatorExpression);
        } catch (ExpressionSyntaxErrorException e) {
            String errorMsg = String.format("执行 Aviator 表达式错误，错误信息：[%s]", e.getMessage());
            throw new DynamicSummaryException(errorMsg, e);
        }
        return (null == result) ? "" : result.toString();
    }

    /**
     * 传入正则表达式，判断一个字符串中与正则表达式匹配的个数
     *
     * @param src     待判断字符串
     * @param pattern 正则表达式
     * @return
     */
    public static int matchRegexCount(String src, Pattern pattern) {
        int count = 0;
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
