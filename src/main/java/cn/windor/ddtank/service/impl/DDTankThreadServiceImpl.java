package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.service.DDtankThreadService;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DDTankThreadServiceImpl implements DDtankThreadService {

    @Autowired
    private Library dm;

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_SUSPEND = 2; // 模拟手动结束任务快捷键
    public static final int SHORTCUT_STOP = 3;

    private final Map<Long, Thread> threadMap = new ConcurrentHashMap<>();

    public DDTankThreadServiceImpl(Library dm) {
        this.dm = dm;
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().registerHotKey(SHORTCUT_SUSPEND,
                JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().addHotKeyListener(new DDtankHotkeyListener()); // 添加监听
        log.info("正在监听快捷键");
    }

    @Override
    public void changeStartShortcut(int modifier, char keycode) {
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, modifier, keycode);
    }

    @Override
    public void removeStartShortcut() {
        JIntellitype.getInstance().unregisterHotKey(SHORTCUT_START); // 移除快捷键 FIRST_SHORTCUT
    }

    @Override
    public Map<Long, Thread> getAllStartedThreadMap() {
        return this.threadMap;
    }

    class DDtankHotkeyListener implements HotkeyListener {

        @Override
        public void onHotKey(int identifier) {
            switch (identifier) {
                case SHORTCUT_START: {
                    log.info("快捷键 alt + 1 被触发，任务开始");
                    synchronized (DDTankThreadServiceImpl.class) {
                        long hwnd = dm.getMousePointWindow();
                        if (threadMap.get(hwnd) == null) {
                            Thread thread = new Thread(() -> {
                                while(true) {
                                    try {
                                        Thread.sleep(10000);
                                        log.info("运行中");
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                            threadMap.put(hwnd, thread);
                            thread.start();
                        }else{
                            log.warn("当前窗口已被添加到运行库中！");
                        }
                    }
                    break;
                    // 开启子线程
                }
                case SHORTCUT_SUSPEND: {
                    log.info("快捷键 ctrl + ALT + 1 被触发，任务结束");
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }
}
