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

    public String newestLog() {
        if(logs.size() == 0) {
            return "当前还没有记录任何日志哦！";
        }
        return logs.get(logs.size() - 1).msg;
    }

    public void log(String str) {
        if(logs.size() == maxSize) {
            logs.remove(0);
        }
        logs.add(new Log(str));
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
