package com.jasolar.mis.framework.common.util.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.Data;

/**
 * 用于在JAVA代码中计算SpEL表达式的值, 字符串中的 ${} 为需要计算的表达式.
 * 注意如果root中有map字段, 需要通过map['key']的形式获取
 * 
 * @author galuo
 * @date 2022/05/18
 */
public class SpelUtils {

    /** SpelExpressionParser是线程安全 */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    /** 解析模板 */
    private static final TemplateParserContext PARSER_CONTEXT = new TemplateParserContext("${", "}");

    /**
     * 
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     *
     * @param <R> 返回的数据类型
     * @param expectedType 返回的数据类型
     * @param expression 表达式， 如：${ a + b }
     * @param root 读取数据的根对象, 直接通过字段名读取
     * @param vars 可使用的变量,通过"#变量名"读取
     * @return 表达式计算后的值
     */
    public static <R> R getValue(Class<R> expectedType, String expression, Object root, Map<?, ?> vars) {
        Expression exp = PARSER.parseExpression(expression, PARSER_CONTEXT);
        StandardEvaluationContext context = root == null ? new StandardEvaluationContext() : new StandardEvaluationContext(root);
        if (vars != null) {
            vars.forEach((k, v) -> context.setVariable(k instanceof String s ? s : Objects.toString(k), v));
        }
        return (R) exp.getValue(context, expectedType);
    }

    /**
     * 
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     *
     * @param <R> 返回的数据类型
     * @param expectedType 返回的数据类型
     * @param expression 表达式， 如：${ a + b }
     * @param root 读取数据的根对象, 直接通过字段名读取
     * @return 表达式计算后的值
     */
    public static <R> R getValue(Class<R> expectedType, String expression, Object root) {
        return getValue(expectedType, expression, root, null);
    }

    /**
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     * 
     * @param expression 表达式， 如：${ a + b + #c }
     * @param root 读取数据的根对象, 直接通过字段名读取
     * @return 表达式计算后的值
     */
    public static Object getValue(String expression, Object root) {
        return getValue(Object.class, expression, root);
    }

    /**
     * 
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     *
     * @param <R> 返回的数据类型
     * @param expression 表达式， 如：${ a + b }
     * @param root 读取数据的根对象, 直接通过字段名读取
     * @param vars 可使用的变量,通过"#变量名"读取
     * @return 表达式计算后的值
     */
    public static Object getValue(String expression, Object root, Map<?, ?> vars) {
        return getValue(Object.class, expression, root, vars);
    }

    /**
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     *
     * @param <R> 返回的数据类型
     * @param expectedType 返回的数据类型
     * @param expression 表达式， 如： ${ #a + #b }
     * @param vars 可使用的变量,通过"#变量名"读取
     * @return 表达式计算后的值
     */
    public static <R> R getValue(Class<R> expectedType, String expression, Map<?, ?> vars) {
        return getValue(expectedType, expression, null, vars);
    }

    /**
     * 计算表达式的值。 注意表达式中引用变量时直接用字段名即可
     *
     * @param expression 表达式， 如：${ #a + #b }
     * @param vars 可使用的变量,通过"#变量名"读取
     * @return 表达式计算后的值
     */
    public static Object getValue(String expression, Map<?, ?> vars) {
        return getValue(Object.class, expression, vars);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "x");

        A a = new A();
        a.setA(0);
        a.setC("c");
        a.setM(map);

        A b = new A();
        b.a = 111;

        A c = new A();
        c.a = 222;
        A d = new A();
        d.a = 111;

        a.setChildren(Arrays.asList(b, c, d));
        List<Integer> set = (List<Integer>) getValue(
                "${ T(com.fiifoxconn.mis.framework.common.util.collection.CollectionUtils).distinct( children.![ a ] ) }", a);
        System.out.println(set);
    }

    @Data
    static class A {
        int a;
        String c;
        Map<String, Object> m;

        List<A> children = new ArrayList<>();

        public int getCol(int x) {
            return 22;
        }
    }

}
