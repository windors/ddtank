package cn.windor.ddtank.account;

/**
 * 在自动重连时如果没有自动进行登录，那么如何进行自动登录操作
 */
public interface DDTankAccountSignHandler {
    void setUsername(String username);

    void setPassword(String password);

    String getUsername();

    String getPassword();


    /**
     * 需要先绑定窗口
     */
    void login();
}
