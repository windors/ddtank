package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.DDTankStuckCheckDetectionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDTankStuckCheckDetectionByLog implements DDTankStuckCheckDetectionHandler {

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

        Map<String, Integer> msgMap = new HashMap<>();
        int size = 0;
        for (DDTankLog.Log log : ddTankLog.getLogs()) {
            if(log.getMsg().contains("自动重连")) {
                // 若过去的时间节点中已经自动重连过，则暂时认为没有卡住。
                return false;
            }
            if (log.getTime().isAfter(begin)) {
                String key = log.getMsg();
                msgMap.merge(key, 1, Integer::sum);
                size++;
            } else {
                // 我定义的日志集合是按时间顺序排列的，所以遇到第一个不满足的后面的就都不满足
                break;
            }
        }
        if(size >= checkTime / 3) {
            // 日志输出速度超过了3秒钟1条日志时，极有可能卡住了，此时认为任何一条日志超过1/6，则判断卡死。
            for (Integer value : msgMap.values()) {
                if ((double) value / (double) size >  1.0 / 6) {
                    return true;
                }
            }
        } else if(size >= 10) {
            for (Integer value : msgMap.values()) {
                // 某条信息出现的次数超过了4成，认为当前日志在一直循环某个操作
                if ((double) value / (double) size > 0.4) {
                    return true;
                }
            }
        }

        return false;
    }
}
