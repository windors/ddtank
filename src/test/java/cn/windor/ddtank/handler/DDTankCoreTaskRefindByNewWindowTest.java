package cn.windor.ddtank.handler;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.impl.DDTankCoreTaskRefindByNewWindow;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import org.junit.jupiter.api.Test;

import java.util.List;


public class DDTankCoreTaskRefindByNewWindowTest {
    @Test
    public void hwndsFindTest() {
        DMLibrary dm = new DMLibrary(LibraryFactory.getActiveXCompnent());
        String className = "Afx:00400000:8:00010003:00000006:00000000";
        List<Long> hwnds = dm.enumWindow(0, "", className, 2 | 8 | 16);

        System.out.println(hwnds);
    }

    /**
     * 测试功能
     */
    @Test
    public void openNewHwndTest() {
        ActiveXComponent compnent = LibraryFactory.getActiveXCompnent();
        Library dm = new DMLibrary(compnent);
        Keyboard keyboard = new DMKeyboard(compnent);
        Mouse mouse = new DMMouse(compnent);

        DDTankConfigProperties properties = new DDTankConfigProperties();
        properties.setWebsite("http://baidu.com");
        DDTankCoreTaskRefindByNewWindow ddTankCoreTaskRefindByNewWindow = new DDTankCoreTaskRefindByNewWindow(dm, new DDTankLog(), properties);
        ddTankCoreTaskRefindByNewWindow.setUsername("test");
        ddTankCoreTaskRefindByNewWindow.setPassword("test");
        long newHwnd = ddTankCoreTaskRefindByNewWindow.refindHwnd(1445114);
        System.out.println("新的游戏句柄：" + newHwnd);
        ComThread.Release();
    }
}