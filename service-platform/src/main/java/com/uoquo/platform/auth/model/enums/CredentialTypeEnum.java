package com.uoquo.platform.auth.model.enums;

public enum CredentialTypeEnum {
    WEIXIN("weixin"),
    WECOM("wecom");

    private final String code;

    CredentialTypeEnum(String code) { this.code = code; }

    public String getCode() { return code; }

    public static boolean contains(String code) {
        for (CredentialTypeEnum e : values()) {
            if (e.code.equals(code)) return true;
        }
        return false;
    }
}
