package cn.windor.ddtank.core;

public class DDtankCoreImpl implements Runnable {
    private long hwnd;

    private int round = 0;

    private int times = 0;

    private String msg;

    // 小地图左上和右下点
    private int staticX1, staticX2, staticY1, staticY2;

    private String roleColor;

//    private DMLibrary dm;
//
//    public DDtankCoreImpl(ActiveXComponent dm) {
//        this.dm = new DMLibrary(dm);
//    }
//
//    public boolean init() {
//        hwnd = dm.getMousePointWindow();
//        if("MacromediaFlashPlayerActiveX".equals(dm.getWindowClass(hwnd))) {
//            this.staticX1 = 794;
//            this.staticY1 = 23;
//            this.staticX2 = 1000;
//            this.staticY2 = 122;
//            this.roleColor = "0033cc";
//            return true;
//        }
//        return false;
//    }

    @Override
    public void run() {
//        if(init()) {
            while (true) {

            }
//        }
    }
}
