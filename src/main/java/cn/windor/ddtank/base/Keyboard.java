package cn.windor.ddtank.base;


public interface Keyboard {

    /**
     * @param k k只能是数字、大小写字母
     */
    void keyDown(char k);

    void keyUp(char k);

    void keyPress(char k);

    void keysPress(String str);

    void keysPress(String str, long millis);

    void keyPressChar(String k);

    void keyDownChar(String k);

    void keyUpChar(String k);

    void keysPressStr(String str);
}
