package cn.windor.ddtank.core;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class DDTankLog {
    private final LocalDateTime createTime;

    private final int maxSize;

    @Getter
    private List<Log> logs;

    public DDTankLog() {
        this(Integer.MAX_VALUE);
    }

    public DDTankLog(int maxSize) {
        this.maxSize = maxSize;
        this.createTime = LocalDateTime.now();
        logs = new LinkedList<>();
    }

    public Log newestLog() {
        if(logs.size() == 0) {
            return new Log("当前还没有记录任何日志哦！");
        }
        return logs.get(0);
    }

    public void info(String str) {
        checkSize();
        logs.add(0, new Log(str));
    }

    public void success(String str) {
        checkSize();
        logs.add(0, new Log(str));
    }

    public void warn(String str) {
        checkSize();
        logs.add(0, new Log(str));
    }

    public void error(String str) {
        checkSize();
        logs.add(0, new Log(str));
    }

    private void checkSize() {
        if(logs.size() == maxSize) {
            logs.remove(logs.size() - 1);
        }
    }

    static class Log {
        @Getter
        private final LocalDateTime time;

        @Getter
        private final String msg;

        public Log(String msg) {
            this.time = LocalDateTime.now();
            this.msg = msg;
        }
    }
}
