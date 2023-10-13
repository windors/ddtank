package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.core.impl.DDtankOperate10_4;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankAngleAdjust;
import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.DDtankCoreThread;
import cn.windor.ddtank.core.impl.DMDDtankPic10_4;
import cn.windor.ddtank.core.impl.SimpleDDTankAngleAdjust;
import cn.windor.ddtank.service.DDTankThreadService;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DDTankThreadServiceImpl implements DDTankThreadService {

    @Autowired
    private Library dm;

    @Autowired
    private DDTankConfigProperties properties;

    @Autowired
    private Keyboard keyboard;

    @Autowired
    private Mouse mouse;

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_SUSPEND = 2; // 模拟手动结束任务快捷键
    public static final int SHORTCUT_STOP = 3;

    private final Map<Long, DDtankCoreThread> threadMap = new ConcurrentHashMap<>();

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
    public Map<Long, DDtankCoreThread> getAllStartedThreadMap() {
        return this.threadMap;
    }

    class DDtankHotkeyListener implements HotkeyListener {
        @Override
        public void onHotKey(int identifier) {
            switch (identifier) {
                case SHORTCUT_START: {
                    log.info("启动快捷键被触发，开始启动脚本");
                    synchronized (DDTankThreadServiceImpl.class) {
                        long hwnd = dm.getMousePointWindow();
                        if ("MacromediaFlashPlayerActiveX".equals(dm.getWindowClass(hwnd))) {
                            synchronized (this.getClass()) {
                                DDtankCoreThread ddtankCoreThread = threadMap.get(hwnd);
                                if (ddtankCoreThread != null && ddtankCoreThread.isAlive()) {
                                    log.warn("请勿重复启动，当前窗口已被添加到运行库中");
                                } else {
                                    // 启动脚本线程
                                    DDTankPic ddTankPic = new DMDDtankPic10_4(dm, "C:/tmp/", properties, mouse);
                                    DDTankOperate ddTankOperate = new DDtankOperate10_4(dm, mouse, keyboard, ddTankPic, properties);
                                    DDTankAngleAdjust ddTankAngleAdjust = new SimpleDDTankAngleAdjust(keyboard);
                                    DDtankCoreThread thread = new DDtankCoreThread(hwnd, dm, ddTankPic, ddTankOperate, properties, ddTankAngleAdjust);
                                    threadMap.put(hwnd, thread);
                                    thread.start();
                                }
                            }
                        } else {
                            log.warn("当前窗口类名不为[MacromediaFlashPlayerActiveX]！");
                        }
                    }
                    break;
                    // 开启子线程
                }
                case SHORTCUT_SUSPEND: {
                    log.info("终止快捷键被触发，任务结束");
                    long hwnd = dm.getMousePointWindow();
                    DDtankCoreThread ddtankCoreThread = threadMap.remove(hwnd);
                    if (ddtankCoreThread == null) {
                        log.error("当前线程已经被停止");
                    } else {
                        ddtankCoreThread.interrupt();
                        log.info("已尝试停止该线程");
                    }
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }
}