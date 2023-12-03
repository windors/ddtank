package cn.windor.ddtank.util;

import com.jacob.activeX.ActiveXComponent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class ActiveXComponentUtils {
    /**
     * 更新obj对象中，所有用到的ActiveXComponent对象
     * @param obj 待更新的obj对象
     * @param supplier 提供ActiveXComponent对象的匿名内部类
     * @return 是否检测并进行过替换
     */
    public static boolean update(Object obj, Supplier<ActiveXComponent> supplier) {
        try {
            return update(obj, supplier, new HashSet<>());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean update(Object obj, Supplier<ActiveXComponent> supplier, Set<Object> checkedSet) throws IllegalAccessException {
        if (obj == null || obj.getClass() == Object.class) {
            return false;
        }
        // 检测当前对象自己及父类中是否用到了ActiveXComponent对象
        boolean result = updateInherit(obj, obj.getClass(), supplier);
        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            // 检测继承链中是否用到了ActiveXComponent
            if (updateInherit(obj, clazz, supplier)) {
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
                if (o.getClass() == ActiveXComponent.class) {
                    field.set(obj, supplier.get());
                    result = true;
                } else {
                    if (update(o, supplier, checkedSet)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 检查obj对象在clazz下是否有ActiveXComponent接口
     * @param clazz obj的继承链中的
     */
    private static boolean updateInherit(Object obj, Class<?> clazz, Supplier<ActiveXComponent> supplier) throws IllegalAccessException {
        if (obj == null) {
            return false;
        }
        boolean result = false;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == ActiveXComponent.class) {
                field.setAccessible(true);
                field.set(obj, supplier.get());
                result = true;
            }
        }
        return result;
    }
}
