package cn.windor.ddtank.util;

import com.jacob.com.Variant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VariantUtils {
    private static final ThreadLocal<Map<Object, Variant>> paramMap = new ThreadLocal<>();

    public static Variant getParam(Object param) {
        Map<Object, Variant> threadParamMap = paramMap.get();
        // 若当前线程没有则new一个对象
        if(threadParamMap == null) {
            synchronized (Thread.currentThread().getName()) {
                threadParamMap = paramMap.get();
                if(threadParamMap == null) {
                    threadParamMap = new ConcurrentHashMap<>();
                    paramMap.set(threadParamMap);
                }
            }
        }

        // 获取Variant对象
        if (threadParamMap.get(param) != null) {
            return threadParamMap.get(param);
        }
        Variant result;
        synchronized (paramMap) {
            if ((result = threadParamMap.get(param)) == null) {
                result = new Variant(param);
                threadParamMap.put(param, result);
            }
            return result;
        }
    }

    /**
     * 清空当前线程的所有Variant对象
     */
    public static void remove() {
        paramMap.remove();
    }
}
