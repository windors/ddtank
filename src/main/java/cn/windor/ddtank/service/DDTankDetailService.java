package cn.windor.ddtank.service;

public interface DDTankDetailService {
    boolean setTaskAutoComplete(long hwnd, int taskAutoComplete);

    boolean setAutoUseProp(long hwnd, int autoUseProp);
}
