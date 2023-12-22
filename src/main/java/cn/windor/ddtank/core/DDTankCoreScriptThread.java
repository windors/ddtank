package cn.windor.ddtank.core;

import cn.windor.ddtank.type.CoreThreadStateEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DDTankCoreScriptThread extends Thread {

    @Getter
    private final DDTankCoreScript script;


    public DDTankCoreScriptThread(DDTankCoreScript script) {
        this.script = script;
        setName(script.getName());
    }


    @Override
    public void run() {
        script.run();
    }

    /**
     * 停止操作是线程安全的，因为isAlive()方法并不受线程上下文干扰
     */
    public void stop(long waitMillis) {
        tryStop();
        try {
            script.coreThread.join(waitMillis);
            this.join(waitMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (script.coreThread.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", script.coreThread.getName(), waitMillis);
        }
        if (this.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", this.getName(), waitMillis);
        }
    }

    public void tryStop() {
        log.info("{}尝试停止操作", script.getName());
        if (script.coreThread.isAlive() || this.isAlive()) {
            script.task.coreState.set(CoreThreadStateEnum.WAITING_STOP);
        }

        if (script.coreThread.isAlive()) {
            script.coreThread.interrupt();
        }
        if (this.isAlive()) {
            this.interrupt();
        }
    }

    public void stopForced() {
        stop();
    }
}
