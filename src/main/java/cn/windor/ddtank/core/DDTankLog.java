package cn.windor.ddtank.core;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class DDTankLog implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public void primary(String str) {
        checkSize();
        logs.add(0, new PrimaryLog(str));
    }

    public void success(String str) {
        checkSize();
        logs.add(0, new SuccessLog(str));
    }

    public void warn(String str) {
        checkSize();
        logs.add(0, new WarnLog(str));
    }

    public void error(String str) {
        checkSize();
        logs.add(0, new ErrorLog(str));
    }

    private void checkSize() {
        if(logs.size() == maxSize) {
            logs.remove(logs.size() - 1);
        }
    }

    public static class Log implements Serializable {

        private static final long serialVersionUID = 1L;

        @Getter
        private final LocalDateTime time;

        @Getter
        private final String msg;

        public Log(String msg) {
            this.time = LocalDateTime.now();
            this.msg = msg;
        }
    }


    static class SuccessLog extends Log {

        public SuccessLog(String msg) {
            super("<span class=\"log-success\">" + msg + "</span>");
        }
    }
    static class PrimaryLog extends Log {

        public PrimaryLog(String msg) {
            super("<span class=\"log-primary\">" + msg + "</span>");
        }
    }

    static class WarnLog extends Log {

        public WarnLog(String msg) {
            super("<span class=\"log-warn\">" + msg + "</span>");
        }
    }

    static class ErrorLog extends Log {

        public ErrorLog(String msg) {
            super("<span class=\"log-error\">" + msg + "</span>");
        }
    }
}
