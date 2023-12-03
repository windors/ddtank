package cn.windor.ddtank.util;

import cn.windor.ddtank.exception.StopTaskException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtils {
    /**
     * 线程休眠方法，被打断则重新设置打断状态
     *
     * @param millis
     * @param exitDirect 中断后是否直接退出，若为true则会抛出StopTaskException异常，被Task接收后会进入catch代码块，就不会执行后面的方法了
     */
    public static void delay(long millis, boolean exitDirect) {
        if (millis < 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (exitDirect) {
                throw new StopTaskException();
            }
        }
    }

    /**
     * 不被中断干扰的延迟方法，并且在延迟过后设回中断
     *
     * @param millis
     */
    public static void delayPersisted(long millis, boolean exitDirect) {
        long start = System.currentTimeMillis();
        try {
            if (millis > 0) {
                log.info("等待。。。");
                Thread.sleep(millis);
                log.info("等待结束...");
            }
        } catch (Exception e) {
            long need = millis - (System.currentTimeMillis() - start);
            while (need > 0) {
                start = System.currentTimeMillis();
                delayPersisted(need, exitDirect);
                need -= System.currentTimeMillis() - start;
            }
            Thread.currentThread().interrupt();
            if (exitDirect) {
                throw new StopTaskException();
            }
        }
    }
}