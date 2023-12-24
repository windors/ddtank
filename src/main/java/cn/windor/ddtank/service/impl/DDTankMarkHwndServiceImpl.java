package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.handler.DDTankHwndMarkHandler;
import cn.windor.ddtank.handler.impl.DDTankHwndMarkHandlerImpl;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankThreadService;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 窗口标记服务类
 * 负责窗口标记，标记后创建DDTankCoreScript对象
 */
@Service
@Slf4j
public class DDTankMarkHwndServiceImpl implements DDTankMarkHwndService {

    private static final AtomicInteger nameId = new AtomicInteger(0);

    private final Library dm;

    @Autowired
    private DDTankCoreTaskProperties defaultProperties;

    @Autowired
    private DDTankThreadService threadService;

    // 在标记时会创建Script对象，启动时直接调用即可
    private static final Map<Long, DDTankCoreScript> markedHwndScriptMap = new ConcurrentHashMap<>();

    private final cn.windor.ddtank.handler.DDTankHwndMarkHandler DDTankHwndMarkHandler;

    public DDTankMarkHwndServiceImpl(Library dm) {
        this.dm = dm;
        DDTankHwndMarkHandler = new DDTankHwndMarkHandlerImpl(dm);
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().registerHotKey(SHORTCUT_STOP,
                JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().addHotKeyListener(new DDtankHotkeyListener()); // 添加监听
        log.info("正在监听快捷键");
    }

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_STOP = 2; // 模拟手动结束任务快捷键


    @Override
    public Map<Long, DDTankCoreScript> getWaitStartMap() {
        for (Long hwnd : markedHwndScriptMap.keySet()) {
            if (!dm.getWindowState(hwnd, 0)) {
                log.warn("检测到窗口关闭，已自动移除待启动脚本");
                markedHwndScriptMap.remove(hwnd);
            }
        }
        return markedHwndScriptMap;
    }

    @Override
    public DDTankCoreScript get(long hwnd) {
        return markedHwndScriptMap.get(hwnd);
    }

    @Override
    public DDTankCoreScript removeByHwnd(long hwnd) {
        return markedHwndScriptMap.remove(hwnd);
    }

    @Override
    public void changeStartShortcut(int modifier, char keycode) {
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, modifier, keycode);
    }

    @Override
    public void removeStartShortcut() {
        JIntellitype.getInstance().unregisterHotKey(SHORTCUT_START); // 移除快捷键 FIRST_SHORTCUT
    }

    private String nextName() {
        return "脚本" + nameId.incrementAndGet();
    }

    @Override
    public Long mark() {
        long hwnd = dm.getMousePointWindow();
        if (DDTankHwndMarkHandler.isLegalHwnd(hwnd)) {
            long legalHwnd = DDTankHwndMarkHandler.getLegalHwnd(hwnd);
            synchronized (this) {
                DDTankCoreScript script = DDTankThreadServiceImpl.getRunningScript(hwnd);
                if (script != null) {
                    log.info("窗口[{}]已记录在首页中，对应脚本名称为[{}]；若需启动当前脚本请在首页点击重启按钮；若需要更换版本配置请在首页手动将脚本移除后再次尝试", hwnd, script.getName());
                } else if (markedHwndScriptMap.get(hwnd) == null) {
                    String name = nextName();
                    if (legalHwnd == hwnd) {
                        log.info("已成功记录当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                        markedHwndScriptMap.put(hwnd, new DDTankCoreScript(hwnd, name, defaultProperties.clone(),false));
                    } else {
                        log.warn("当前窗口[{}]为前台模式（脚本启动后鼠标和键盘操作都将变为前台），请勿随意调整窗口大小和最小化。若需要使用后台模式请将浏览器设置为兼容模式", hwnd);
                        if (markedHwndScriptMap.get(legalHwnd) == null) {
                            markedHwndScriptMap.put(legalHwnd, new DDTankCoreScript(hwnd, name, defaultProperties.clone(), true));
                            log.info("已记录当前顶层窗口{}", legalHwnd);
                        } else {
                            log.info("当前标签栏{}已被记录，前台模式多开请将标签栏拖出为一个新的窗口", legalHwnd);
                        }
                    }
                } else {
                    log.info("已记录过当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                }
            }
        } else {
            log.warn("当前窗口未通过脚本预设值！");
        }
        return hwnd;
    }

    @Override
    public Long getLegalHwnd(long hwnd) {
        // 1. 尝试标记 newHwnd
        if (!DDTankHwndMarkHandler.isLegalHwnd(hwnd)) {
            log.error("重绑定失败，检测到窗口{}非法！", hwnd);
            return 0L;
        }

        return DDTankHwndMarkHandler.getLegalHwnd(hwnd);
    }

    class DDtankHotkeyListener implements HotkeyListener {
        @Override
        public void onHotKey(int identifier) {
            switch (identifier) {
                case SHORTCUT_START: {
                    mark();
                    break;
                }
                case SHORTCUT_STOP: {
                    long hwnd = dm.getMousePointWindow();
                    threadService.stop(Collections.singletonList(threadService.get(hwnd)));
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }

}
