package com.jasolar.mis.module.system.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树形结构转换工具类
 */
public class TreeConvertUtil {
    
    /**
     * 将平面列表转换为树形结构的泛型方法
     * 
     * @param flatList 平面列表
     * @param getCodeFunc 获取节点编码的函数
     * @param getParentCodeFunc 获取父节点编码的函数
     * @param getChildrenFunc 获取子节点列表的函数
     * @param setChildrenFunc 设置子节点列表的函数
     * @param <T> 节点类型
     * @return 树形结构列表
     */
    public static <T> List<T> convertToTree(
            List<T> flatList,
            Function<T, String> getCodeFunc,
            Function<T, String> getParentCodeFunc,
            Function<T, List<T>> getChildrenFunc,
            java.util.function.BiConsumer<T, List<T>> setChildrenFunc) {
        
        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, T> nodeMap = flatList.stream()
                .collect(Collectors.toMap(getCodeFunc, node -> node, (existing, replacement) -> existing));
        
        List<T> rootNodes = new ArrayList<>();
        
        for (T node : flatList) {
            String parentCode = getParentCodeFunc.apply(node);
            
            if (parentCode == null || parentCode.isEmpty()) {
                if (getChildrenFunc.apply(node) == null) {
                    setChildrenFunc.accept(node, new ArrayList<>());
                }
                rootNodes.add(node);
            } else {
                T parentNode = nodeMap.get(parentCode);
                if (parentNode != null) {
                    if (getChildrenFunc.apply(parentNode) == null) {
                        setChildrenFunc.accept(parentNode, new ArrayList<>());
                    }
                    getChildrenFunc.apply(parentNode).add(node);
                } else {
                    // 如果找不到父节点，将当前节点作为根节点
                    if (getChildrenFunc.apply(node) == null) {
                        setChildrenFunc.accept(node, new ArrayList<>());
                    }
                    rootNodes.add(node);
                }
            }
        }
        return rootNodes;
    }
}