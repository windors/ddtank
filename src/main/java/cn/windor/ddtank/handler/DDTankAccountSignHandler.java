package cn.windor.ddtank.handler;

/**
 * 在自动重连时如果没有自动进行登录，那么如何进行自动登录操作
 */
public interface DDTankAccountSignHandler {
    /**
     * 需要先绑定窗口
     * @param username
     * @param password
     */
    void login(String username, String password);
}
