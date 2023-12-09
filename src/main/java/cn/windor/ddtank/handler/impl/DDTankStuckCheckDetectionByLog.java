package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.DDTankStuckCheckDetectionHandler;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDTankStuckCheckDetectionByLog implements DDTankStuckCheckDetectionHandler, Serializable {
    private static final long serialVersionUID = 1L;

    private DDTankLog ddTankLog;

    private long checkTime = 240;

    public DDTankStuckCheckDetectionByLog(DDTankLog ddTankLog) {
        this.ddTankLog = ddTankLog;
    }

    @Override
    public boolean isStuck() {
        // 获取时间截点
        LocalDateTime begin = LocalDateTime.now().minusSeconds(checkTime);

        if (ddTankLog.newestLog().getTime().isBefore(begin)) {
            // 日志已经长时间未更新，判断为卡死
            return true;
        }

        List<String> msgs = new ArrayList<>();
        int cycle = 3;
        boolean stopAdd = false;
        int size = 0;

        for (DDTankLog.Log log : ddTankLog.getLogs()) {
            if (log.getMsg().contains("自动重连")) {
                // 若过去的时间节点中已经自动重连过，则暂时认为没有卡住。
                return false;
            }
            if (log.getTime().isAfter(begin)) {
                size++;
                String msg = log.getMsg();
                if(stopAdd || msgs.size() == cycle) {
                    stopAdd = true;
                }else {
                    if (!msgs.contains(msg)) {
                        msgs.add(msg);
                    }
                }
                if(stopAdd) {
                    // 如果所有符合时间节点的消息都在msgs中，说明确实卡住，否则期间出现了其他消息说明没有卡住
                    if(!msgs.contains(msg)) {
                        return false;
                    }
                }
            } else {
                // 我定义的日志集合是按时间顺序排列的，所以遇到第一个不满足的后面的就都不满足
                break;
            }
        }

        // 运行到这里说明没有通过循环操作卡死检测
        // 当日志个数超过10时才认为卡住
        return size > 10;
    }
}
