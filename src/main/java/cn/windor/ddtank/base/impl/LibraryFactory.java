package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.util.FileUtils;
import com.jacob.com.ComThread;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class LibraryFactory {
    public static com.jacob.activeX.ActiveXComponent getActiveXCompnent() {
        com.jacob.activeX.ActiveXComponent dm;
        try {
            ComThread.InitSTA();
            dm = new com.jacob.activeX.ActiveXComponent("dm.dmsoft");
            String ver = dm.invoke("ver").toString();
            if (!"7.2336".equals(ver)) {
                ComThread.Release();
                throw new com.jacob.com.ComFailException();
            }
        } catch (com.jacob.com.ComFailException e) {
            try {
                FileUtils.putAttachment("library\\dm_7.2336.dll", new File("c:\\tmp"));
                Runtime.getRuntime().exec("regsvr32 c:\\tmp\\dm_7.2336.dll");
                log.info("等待注册大漠对象到系统...");
                dm = new com.jacob.activeX.ActiveXComponent("dm.dmsoft");
                String ver = dm.invoke("ver").toString();
                if (!"7.2336".equals(ver)) {
                    throw new RuntimeException("大漠插件注册失败：版本不同");
                }
            } catch (Exception ex) {
                log.info("大漠插件注册失败，请手动注册。若已弹出注册成功则再次启动程序即可");
                throw new RuntimeException(ex);
            }
        }


        int zc = dm.invoke("reg",
                new com.jacob.com.Variant("mh84909b3bf80d45c618136887775ccc90d27d7"), new com.jacob.com.Variant("m88i5mlsu6lh9y7")).getInt();
        if (zc == 1) {
            log.info("大漠插件注册成功");
        } else {
            switch (zc) {
                case -2:
                    log.error("大漠插件注册失败，错误原因：进程没有以管理员方式运行. (出现在win7 win8 vista 2008.建议关闭uac)");
                    break;
                case 0:
                    log.error("大漠插件注册失败，错误原因：失败 (未知错误原因是写的代码不对。注册之前没有创建对象。或者没有注册到系统。如果用的免注册到系统可能代码不对。鉴别方法很简单。在代码里ver输出一下大漠版本号如果是空。说明没有注册到系统或免注册系统代码不对)");
                    break;
                case 2:
                    log.error("大漠插件注册失败，错误原因：余额不足");
                    break;
                case 3:
                    log.error("大漠插件注册失败，错误原因：绑定了本机器，但是账户余额不足50元.");
                    break;
                case 4:
                    log.error("大漠插件注册失败，错误原因：注册码错误");
                    break;
                case 5:
                    log.error("大漠插件注册失败，错误原因：你的机器或者IP在黑名单列表中或者不在白名单列表中.");
                    break;
                case 6:
                    log.error("大漠插件注册失败，错误原因：非法使用插件.");
                    break;
                case 7:
                    log.error("大漠插件注册失败，错误原因：你的帐号因为非法使用被封禁. （如果是在虚拟机中使用插件，必须使用Reg或者RegEx，不能使用RegNoMac或者RegExNoMac,否则可能会造成封号，或者封禁机器）");
                    break;
                case 8:
                    log.error("大漠插件注册失败，错误原因：ver_info不在你设置的附加白名单中，或者余额不足被系统拉黑。如果你是直接买的注册码.可能是余额不足.请下载余额查询工具查询余额下载地址www.52hsxx.com/zhuce.");
                    break;
                case 77:
                    log.error("大漠插件注册失败，错误原因：机器码或者IP因为非法使用，而被封禁. （如果是在虚拟机中使用插件，必须使用Reg或者RegEx，不能使用RegNoMac或者RegExNoMac,否则可能会造成封号，或者封禁机器）封禁是全局的，如果使用了别人的软件导致77，也一样会导致所有注册码均无法注册。解决办法是更换IP，更换MAC.");
                    break;
                case -8:
                    log.error("大漠插件注册失败，错误原因：版本附加信息长度超过了,如果你的版本附加码填写正确.说明你系统里注册使用的大漠版本太老了.请用ver命令输出版本号看看.尽量用7.xx以后的版本.20");
                    break;
                case -9:
                    log.error("大漠插件注册失败，错误原因：版本附加信息里包含了非法字母,请检查是否有空格什么的");
                    break;
                default:
                    log.error("大漠插件注册失败，错误码：{}", zc);
            }
            throw new RuntimeException("大漠插件注册失败");
        }
        return dm;
    }
}
