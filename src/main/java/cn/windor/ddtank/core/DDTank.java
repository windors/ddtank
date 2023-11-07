package cn.windor.ddtank.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DDTank {
    protected DDTankLog ddtLog;

    public DDTank() {
        ddtLog = new DDTankLog();
    }

    public void log(String msg) {
        ddtLog.log(msg);
        log.debug(msg);
    }

    public void logInfo(String msg) {
        ddtLog.log(msg);
        log.info(msg);
    }

    public void logWarn(String msg) {
        ddtLog.log(msg);
        log.warn(msg);
    }

    public void logError(String msg) {
        ddtLog.log(msg);
        log.error(msg);
    }

    public DDTankLog.Log getCurrentLog() {
        return ddtLog.newestLog();
    }



}
