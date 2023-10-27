package cn.windor.ddtank.util;

import cn.windor.ddtank.exception.StopTaskException;

public class ThreadUtils {
    /**
     * 线程休眠方法，被打断则重新设置打断状态
     * @param millis
     * @param exitDirect 中断后是否直接退出，若为true则会抛出StopTaskException异常，被Task接收后会进入catch代码块，就不会执行后面的方法了
     */
    public static void delay(long millis, boolean exitDirect) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if(exitDirect) {
                throw new StopTaskException();
            }
        }
    }

    /**
     * 不被中断干扰的延迟方法，并且在延迟过后设回中断
     * @param millis
     */
    public static void delayPersisted(long millis, boolean exitDirect) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            long need = System.currentTimeMillis() - start;
            while(need > 0) {
                start = System.currentTimeMillis();
                try {
                    Thread.sleep(need);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                need = System.currentTimeMillis() - start;
            }
            Thread.currentThread().interrupt();
            if(exitDirect) {
                throw new StopTaskException();
            }
        }
    }
}