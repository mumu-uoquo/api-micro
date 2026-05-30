package com.uoquo.platform.auth.model.pojo;

/**
 * 账户事件
 * @author xuhz
 */
public class AuthInfo {

    /**
     * 账户名
     */
    private String account;

    /**
     * 密码（仅认证失败时记录）
     */
    private String password;

    /**
     * 登录设备
     */
    private String deviceInfo;

    /**
     * 操作系统
     */
    private String deviceOs;

    /**
     * UserAgent信息
     */
    private String deviceUa;

    public AuthInfo() {
        super();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getDeviceUa() {
        return deviceUa;
    }

    public void setDeviceUa(String deviceUa) {
        this.deviceUa = deviceUa;
    }
}
