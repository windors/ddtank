package cn.windor.ddtank.util;

import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.core.impl.DDtankOperate2_3;
import com.jacob.activeX.ActiveXComponent;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.SetUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
public class DDTankComplexObjectUpdateUtils {

    private final static Set<Class> skipCheckClassSet = new HashSet<>();

    static {
        skipCheckClassSet.add(Byte.class);
        skipCheckClassSet.add(Byte[].class);
        skipCheckClassSet.add(Short.class);
        skipCheckClassSet.add(Short[].class);
        skipCheckClassSet.add(Integer.class);
        skipCheckClassSet.add(Integer[].class);
        skipCheckClassSet.add(Long.class);
        skipCheckClassSet.add(Long[].class);
        skipCheckClassSet.add(Float.class);
        skipCheckClassSet.add(Float[].class);
        skipCheckClassSet.add(Double.class);
        skipCheckClassSet.add(Double[].class);
        skipCheckClassSet.add(Character.class);
        skipCheckClassSet.add(Character[].class);
        skipCheckClassSet.add(Boolean.class);
        skipCheckClassSet.add(Boolean[].class);
        skipCheckClassSet.add(String.class);
        skipCheckClassSet.add(String[].class);
        skipCheckClassSet.add(Object.class);
        skipCheckClassSet.add(Object[].class);
        skipCheckClassSet.add(ch.qos.logback.classic.Logger.class);
        skipCheckClassSet.add(ch.qos.logback.classic.Level.class);
        skipCheckClassSet.add(CopyOnWriteArrayList.class);
        skipCheckClassSet.add(ConcurrentHashMap.class);
        skipCheckClassSet.add(LocalDateTime.class);
        skipCheckClassSet.add(LocalDate.class);
        skipCheckClassSet.add(LocalTime.class);
        skipCheckClassSet.add(LinkedList.class);
        skipCheckClassSet.add(AtomicReference.class);
        skipCheckClassSet.add(HashSet.class);
        skipCheckClassSet.add(ThreadPoolExecutor.class);
        skipCheckClassSet.add(AtomicInteger.class);
    }
    /**
     * 更新obj对象中，所有用到的ActiveXComponent对象
     * @param obj 待更新的obj对象
     * @return 是否检测并进行过替换
     */
    public static boolean update(Object obj, Object... replaceObjects) {
        try {
            return update(obj, new HashSet<>(), replaceObjects);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean update(Object obj, Set<Object> checkedSet, Object... replaceObjects) throws IllegalAccessException {
        if (obj == null || skipCheckClassSet.contains(obj.getClass())) {
            return false;
        }
        // 检测当前对象自己及父类中是否用到了replaceObjects中的类
        boolean result = updateInherit(obj, obj.getClass(), replaceObjects);
        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            // 检测继承链中是否用到了replaceObjects
            if (updateInherit(obj, clazz, replaceObjects)) {
                result = true;
            }
            clazz = clazz.getSuperclass();
        }

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object o = field.get(obj);
            if(o != null && !checkedSet.contains(o)) {
                checkedSet.add(o);
                // 对当前类的每个字段对象调用该方法，对所有用到的类进行替换
                for (Object replace : replaceObjects) {
                    if (o.getClass() == replace.getClass()) {
                        field.set(obj, replace);
                        result = true;
                    } else {
                        if (update(o, checkedSet, replaceObjects)) {
                            result = true;
                        }
                    }
                }

            }
        }
        return result;
    }


    /**
     * 替换obj对象在clazz下的replaceObjects类
     * @param clazz obj的继承链中的
     */
    private static boolean updateInherit(Object obj, Class<?> clazz, Object... replaceObjects) throws IllegalAccessException {
        if (obj == null || skipCheckClassSet.contains(obj.getClass())) {
            return false;
        }
        boolean result = false;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if(skipCheckClassSet.contains(field.getType())) {
                continue;
            }
            for (Object replace : replaceObjects) {
                if (isParentClass(field.getType(), replace.getClass())) {
                    field.setAccessible(true);
                    field.set(obj, replace);
                    result = true;
                }
            }

        }
        return result;
    }

    /**
     * 类A是否是类B的父类
     * @param a
     * @param b
     * @return
     */
    private static boolean isParentClass(Class a, Class b) {
        if(a.isInterface()) {
            while(b != Object.class) {
                // 查看类b是否实现了a接口
                Class[] interfaces = b.getInterfaces();
                for (Class interfaceClass : interfaces) {
                    if (a == interfaceClass) {
                        return true;
                    }
                }
                b = b.getSuperclass();
            }
        }else {
            if (a == b) {
                return true;
            }
            while (b != Object.class) {
                b = b.getSuperclass();
                if (a == b) {
                    return true;
                }
            }
        }
        return false;
    }
}
