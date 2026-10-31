package com.uoquo.platform.system.model.param;

import com.uoquo.utils.crypto.License;

/**
 * 授权下载信息：在 {@link License.LicenseInfo} 基础上追加 license_apply 表特有的业务字段。
 * <p>序列化后由 RSA 私钥加密返回给院端，院端用公钥解密并写入授权文件。</p>
 */
public class LicenseInfoParam extends License.LicenseInfo {

    /** 授权模块列表（JSON 字符串） */
    private String allowedModules;

    /** 其他授权参数（JSON 字符串） */
    private String allowedExtra;

    public String getAllowedModules() {
        return allowedModules;
    }

    public void setAllowedModules(String allowedModules) {
        this.allowedModules = allowedModules;
    }

    public String getAllowedExtra() {
        return allowedExtra;
    }

    public void setAllowedExtra(String allowedExtra) {
        this.allowedExtra = allowedExtra;
    }
}
