package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.impl.DDtankOperate10_4;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankAngleAdjustMove;
import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.core.impl.DMDDtankPic10_4;
import cn.windor.ddtank.core.impl.SimpleDDTankAngleAdjustMove;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    @Autowired
    private DDTankConfigService configService;

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_SUSPEND = 2; // 模拟手动结束任务快捷键
    public static final int SHORTCUT_STOP = 3;

    private final Map<Long, DDTankCoreThread> threadMap = new ConcurrentHashMap<>();

    private final Map<Long, DDTankStartParam> waitStartMap = new HashMap<>();

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
    public Map<Long, DDTankCoreThread> getAllStartedThreadMap() {
        for (Long hwnd : threadMap.keySet()) {
            if(!dm.getWindowState(hwnd, 0)) {
                log.warn("检测到窗口关闭，已自动移除脚本");
                DDTankCoreThread thread = threadMap.remove(hwnd);
                thread.stop();
            }
        }
        return this.threadMap;
    }

    @Override
    public Map<Long, DDTankStartParam> getWaitStartMap() {
        for (Long hwnd : waitStartMap.keySet()) {
            if(!dm.getWindowState(hwnd, 0)) {
                log.warn("检测到窗口关闭，已自动移除待启动脚本");
                waitStartMap.remove(hwnd);
            }
        }
        return this.waitStartMap;
    }

    @Override
    public synchronized boolean start(long hwnd, int keyboardMode, int mouseMode, int picMode, int operateMode, int propertiesMode, String name) {
        DDTankCoreThread ddtankCoreThread = threadMap.get(hwnd);
        if (ddtankCoreThread != null && ddtankCoreThread.isAlive()) {
            log.warn("请勿重复启动，当前窗口已被添加到运行库中");
            return false;
        } else {
            // 0. 设置键盘鼠标等基本参数
            Keyboard startKeyboard = keyboard;
            Mouse startMouse = mouse;
            DDTankConfigProperties startProperties = properties;
            DDTankAngleAdjustMove startDdTankAngleAdjustMove = new SimpleDDTankAngleAdjustMove(keyboard);
            // 1. 将配置文件克隆
            if(propertiesMode == 0) {
                startProperties = properties.clone();
            }else {
                startProperties = configService.getByIndex(propertiesMode).clone();
            }

            // 2. 根据配置文件创建线程所需对象
            DDTankPic ddTankPic = new DMDDtankPic10_4(dm, "C:/tmp/", startProperties, startMouse);
            DDTankOperate ddTankOperate = new DDtankOperate10_4(dm, startMouse, startKeyboard, ddTankPic, startProperties);

            // 3. 启动线程
            DDTankCoreThread thread = new DDTankCoreThread(hwnd, dm, ddTankPic, ddTankOperate, startProperties, startDdTankAngleAdjustMove);
            threadMap.put(hwnd, thread);
            thread.setName(name);
            thread.start();
            waitStartMap.remove(hwnd);
            return true;
        }
    }

    @Override
    public Long mark() {
        Long hwnd = dm.getMousePointWindow();
        if ("MacromediaFlashPlayerActiveX".equals(dm.getWindowClass(hwnd))) {
            synchronized (this) {
                if (waitStartMap.get(hwnd) == null) {
                    waitStartMap.put(hwnd, new DDTankStartParam());
                    log.info("已成功记录当前窗口，请前往配置页面设置启动参数");
                } else {
                    log.info("已记录过当前窗口，请前往配置页面设置启动参数");
                }
            }
        } else {
            hwnd = null;
            log.warn("当前窗口类名不为[MacromediaFlashPlayerActiveX]！");
        }
        return hwnd;
    }

    @Override
    public void stop(long hwnd) {
        DDTankCoreThread ddtankCoreThread = threadMap.remove(hwnd);
        if (ddtankCoreThread == null) {
            log.error("当前线程已经被停止");
        } else {
            ddtankCoreThread.sendStop();
            log.info("已尝试停止该线程");
        }
    }

    @Override
    public void stopDirectly(long hwnd) {
        DDTankCoreThread ddtankCoreThread = threadMap.remove(hwnd);
        if (ddtankCoreThread == null) {
            log.error("当前线程已经被停止");
        } else {
            ddtankCoreThread.unBind();
            ddtankCoreThread.stop();
        }
    }


    @Override
    public boolean updateProperties(long hwnd, DDTankConfigProperties config) {
        DDTankCoreThread thread = threadMap.get(hwnd);
        if(thread == null || thread.getCoreState() == CoreThreadStateEnum.STOP || thread.getCoreState() == CoreThreadStateEnum.WAITING_STOP) {
            return false;
        }
        thread.updateProperties(config);
        return true;
    }

    class DDtankHotkeyListener implements HotkeyListener {
        @Override
        public void onHotKey(int identifier) {
            switch (identifier) {
                case SHORTCUT_START: {
                    mark();
                    break;
                }
                case SHORTCUT_SUSPEND: {
                    long hwnd = dm.getMousePointWindow();
                    stop(hwnd);
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }
}