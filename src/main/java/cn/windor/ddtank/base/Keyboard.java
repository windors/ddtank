package cn.windor.ddtank.base;

public interface Keyboard {
    void keyDown(char k);

    void keyUp(char k);

    void keyPress(char k);

    void keysPress(String str);
}
