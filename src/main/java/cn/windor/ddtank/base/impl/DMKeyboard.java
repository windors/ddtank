package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.Keyboard;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;

import static cn.windor.ddtank.util.ThreadUtils.delay;
import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

public class DMKeyboard implements Keyboard {

    private ActiveXComponent dm;

    public DMKeyboard(ActiveXComponent dm) {
        this.dm = dm;
    }

    @Override
    public void keyDown(char k) {
        k = Character.toUpperCase(k);
        dm.invoke("keyDown", new Variant(k));
    }

    @Override
    public void keyUp(char k) {
        k = Character.toUpperCase(k);
        dm.invoke("keyUp", new Variant(k));
    }

    @Override
    public void keyPress(char k) {
        k = Character.toUpperCase(k);
        dm.invoke("keyPress", new Variant(k));
    }

    @Override
    public void keysPress(String str, long millis) {
        for (char k : str.toCharArray()) {
            k = Character.toUpperCase(k);
            dm.invoke("keyPress", new Variant(k));
            delayPersisted(millis, true);
        }
    }

    @Override
    public void keysPressStr(String str) {
        dm.invoke("keyPressStr", new Variant(str), new Variant(0));
    }

    @Override
    public void keysPress(String str) {
        keysPress(str, 0);
    }
}
