package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.config.DDTankConfigProperties;
import lombok.extern.slf4j.Slf4j;

import static cn.windor.ddtank.util.ThreadUtils.delay;

/**
 * 守护线程的任务是通过日志勘察脚本线程是否出现问题，同时调用一些其他操作
 */
@Slf4j
public class DDTankDaemonThread extends Thread {

    protected Library dm;

    private long gameHwnd;

    // 需要用到绑定
    private DDTankConfigProperties properties;

    private DDTankCoreThread coreThread;

    public DDTankDaemonThread(DDTankCoreThread coreThread, long gameHwnd, Library dm, DDTankConfigProperties properties) {
        this.coreThread = coreThread;
        this.gameHwnd = gameHwnd;
        this.dm = dm;
        this.properties = properties;
    }

    @Override
    public void run() {
        if(!dm.bindWindowEx(gameHwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            log.error("守护线程绑定窗口失败，一些辅助功能将变得不准确");
        }

        while (true) {
            if(!dm.getWindowState(gameHwnd, 0)) {
                // 调用dm在出错时往往会弹出窗口，所以需要手动关闭线程
                log.error("检测到游戏窗口关闭，即将停止脚本运行");
                coreThread.stop();
                // TODO 后处理
                
            }
            log.info("守护线程运行中");
            delay(10000);
        }
    }

    public boolean screenshot(int x1, int y1, int x2, int y2, String filepath) {
        return dm.capture(x1, y1, x2, y2, filepath);
    }

    public boolean screenshot(String filepath) {
        int[] clientSize = dm.getClientSize(gameHwnd);
        return dm.capture(0, 0, clientSize[0], clientSize[1], filepath);
    }

}
