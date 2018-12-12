package com.tyyd.framework.dat.core.commons.utils;

import java.util.*;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.registry.NodeRegistryUtils;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> List<T> newArrayListOnNull(List<T> list) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        return list;
    }

    public static <T> Set<T> newHashSetOnNull(Set<T> set) {
        if (set == null) {
            set = new HashSet<T>();
        }
        return set;
    }

    public static <K, V> Map<K, V> newHashMapOnNull(Map<K, V> map) {
        if (map == null) {
            map = new HashMap<K, V>();
        }
        return map;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && map.size() > 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return !isNotEmpty(map);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return !isNotEmpty(collection);
    }

    public static <K, V> V getValue(Map<K, V> map , K key){
        if(map == null){
            return null;
        }
        return map.get(key);
    }

    public static int sizeOf(Collection<?> collection) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.size();
    }

    public static int sizeOf(Map<?, ?> map) {
        if (map == null) {
            return 0;
        }
        return map.size();
    }

    /**
     * 返回第一个列表中比第二个多出来的元素
     */
    public static List<Node> getLeftDiff(List<Node> list1, List<Node> list2) {
        if (isEmpty(list2)) {
            return list1;
        }
        List<Node> list = new ArrayList<Node>();
        if (isNotEmpty(list1)) {
            for (Node o : list1) {
                if (!list2.contains(o)) {
                    list.add(o);
                }
            }
        }
        return list;
    }

    public static <T> List<T> setToList(Set<T> set) {
        if (set == null) {
            return null;
        }
        return new ArrayList<T>(set);
    }

    public static <T> List<T> arrayToList(T[] t) {
        if (t == null || t.length == 0) {
            return new ArrayList<T>(0);
        }
        List<T> list = new ArrayList<T>(t.length);
        Collections.addAll(list, t);
        return list;
    }
    /**
     * 返回第一个列表中比第二个多出来的元素
     */
    public static List<Node> getNotDiff(List<Node> list1, List<Node> list2) {
        if (isEmpty(list2)) {
            return null;
        }
        List<Node> list = new ArrayList<Node>();
        if (isNotEmpty(list1)) {
            for (Node o : list1) {
                if (list2.contains(o)) {
                    list.add(o);
                }
            }
        }
        return list;
    }
}
