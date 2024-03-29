/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {
    private static final Comparator<String> SIMPLE_NAME_COMPARATOR = new Comparator<String>(){

        @Override
        public int compare(String s1, String s2) {
            int i2;
            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            int i1 = s1.lastIndexOf(46);
            if (i1 >= 0) {
                s1 = s1.substring(i1 + 1);
            }
            if ((i2 = s2.lastIndexOf(46)) >= 0) {
                s2 = s2.substring(i2 + 1);
            }
            return s1.compareToIgnoreCase(s2);
        }
    };

    public static <T> List<T> sort(List<T> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list);
        }
        return list;
    }

    public static List<String> sortSimpleName(List<String> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list, SIMPLE_NAME_COMPARATOR);
        }
        return list;
    }

    public static Map<String, Map<String, String>> splitAll(Map<String, List<String>> list, String separator) {
        if (list == null) {
            return null;
        }
        HashMap<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, List<String>> entry : list.entrySet()) {
            result.put(entry.getKey(), CollectionUtils.split(entry.getValue(), separator));
        }
        return result;
    }

    public static Map<String, List<String>> joinAll(Map<String, Map<String, String>> map, String separator) {
        if (map == null) {
            return null;
        }
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            result.put(entry.getKey(), CollectionUtils.join(entry.getValue(), separator));
        }
        return result;
    }

    public static Map<String, String> split(List<String> list, String separator) {
        if (list == null) {
            return null;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        if (list == null || list.size() == 0) {
            return map;
        }
        for (String item : list) {
            int index = item.indexOf(separator);
            if (index == -1) {
                map.put(item, "");
                continue;
            }
            map.put(item.substring(0, index), item.substring(index + 1));
        }
        return map;
    }

    public static List<String> join(Map<String, String> map, String separator) {
        if (map == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        if (map == null || map.size() == 0) {
            return list;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.length() == 0) {
                list.add(key);
                continue;
            }
            list.add(key + separator + value);
        }
        return list;
    }

    public static String join(List<String> list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String ele : list) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(ele);
        }
        return sb.toString();
    }

    public static boolean mapEquals(Map<?, ?> map1, Map<?, ?> map2) {
        if (map1 == null && map2 == null) {
            return true;
        }
        if (map1 == null || map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        for (Map.Entry<?, ?> entry : map1.entrySet()) {
            Object value2;
            Object key = entry.getKey();
            Object value1 = entry.getValue();
            if (CollectionUtils.objectEquals(value1, value2 = map2.get(key))) continue;
            return false;
        }
        return true;
    }

    private static boolean objectEquals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    public static /* varargs */ Map<String, String> toStringMap(String ... pairs) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i += 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

    public static /* varargs */ <K, V> Map<K, V> toMap(Object ... pairs) {
        HashMap<Object, Object> ret = new HashMap<Object, Object>();
        if (pairs == null || pairs.length == 0) {
            return ret;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        int len = pairs.length / 2;
        for (int i = 0; i < len; ++i) {
            ret.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return ret;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    private CollectionUtils() {
    }

}

