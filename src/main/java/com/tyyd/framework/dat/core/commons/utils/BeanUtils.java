package com.tyyd.framework.dat.core.commons.utils;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.tyyd.framework.dat.core.exception.DatRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanUtils {

    private static final Map<Class<?>, MethodAccess> METHOD_MAP = new ConcurrentHashMap<Class<?>, MethodAccess>();
    private static final Map<String, Integer> METHOD_INDEX_MAP = new ConcurrentHashMap<String, Integer>();
    private static final Map<Class<?>, List<String>> FIELD_MAP = new ConcurrentHashMap<Class<?>, List<String>>();

    public static void copyProperties(Object desc, Object src) {

        MethodAccess descMethodAccess = METHOD_MAP.get(desc.getClass());

        if (descMethodAccess == null) {
            descMethodAccess = cache(desc);
        }
        MethodAccess srcMethodAccess = METHOD_MAP.get(src.getClass());
        if (srcMethodAccess == null) {
            srcMethodAccess = cache(src);
        }

        List<String> fieldList = FIELD_MAP.get(src.getClass());
        for (String field : fieldList) {
            String getKey = src.getClass().getName() + ".get" + field;
            String setKey = desc.getClass().getName() + ".set" + field;
            Integer setIndex = METHOD_INDEX_MAP.get(setKey);
            if (setIndex != null) {
                int getIndex = METHOD_INDEX_MAP.get(getKey);
                descMethodAccess.invoke(desc, setIndex, srcMethodAccess.invoke(src, getIndex));
            }
        }
    }

    private static MethodAccess cache(Object obj) {

        synchronized (obj.getClass()) {
            MethodAccess methodAccess = MethodAccess.get(obj.getClass());
            Field[] fields = null;
            try {
                // 这里查找了包括父类的属性
                fields = ReflectionUtils.findFields(obj.getClass());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            List<String> fieldList = new ArrayList<String>(fields.length);
            for (Field field : fields) {
                if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    //非公共私有变量
                    String fieldName = capitalize(field.getName());
                    int getIndex = methodAccess.getIndex("get" + fieldName);
                    int setIndex = methodAccess.getIndex("set" + fieldName);
                    METHOD_INDEX_MAP.put(obj.getClass().getName() + ".get" + fieldName, getIndex);
                    METHOD_INDEX_MAP.put(obj.getClass().getName() + ".set" + fieldName, setIndex);
                    fieldList.add(fieldName);
                }
            }
            FIELD_MAP.put(obj.getClass(), fieldList);
            METHOD_MAP.put(obj.getClass(), methodAccess);
            return methodAccess;
        }
    }

    public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            throw new DatRuntimeException(e.getMessage(), e);
        }
    }

    public static String capitalize(String str) {
        return String.valueOf(Character.toTitleCase(str.charAt(0))) +
                str.substring(1);
    }

}
