package cn.windor.ddtank.util;

public class ThreadUtils {
    /**
     * 线程休眠方法，被打断则重新设置打断状态
     * @param millis
     */
    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}