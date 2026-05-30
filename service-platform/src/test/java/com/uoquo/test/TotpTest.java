package com.uoquo.test;

import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.utils.crypto.Base32;
import org.junit.jupiter.api.Test;

public class TotpTest {

    @Test
    public void testTotp() {
        String str = Base32.encode("13800138000");
        System.out.println(str);
        System.out.println(TotpAuthUtils.generateDynamicCode(str));

        str = Base32.encode("abc@123.com");
        System.out.println(str);
        System.out.println(TotpAuthUtils.generateDynamicCode(str));

        str = Base32.encode("abad3ddfddescd-123.DSw@12abcd-23.com.cn");
        System.out.println(str);
        System.out.println(TotpAuthUtils.generateDynamicCode(str));
    }

    @Test
    public void testString() {
        System.out.println("aaa".replaceAll("", "*"));
        // 将字符串全部替换为*
        System.out.println("aaa".replaceAll("(?s).", "*"));
        System.out.println("a1b2c3d4@156.com".replaceAll("(\\w{3}).*@(.*)", "$1****@$2"));

        System.out.println(desensitizeString("a1b2c3d4@156.com", 3, 3, "*", 0));
        System.out.println(desensitizeString("13", 3, 3, "^_^", 0));
        System.out.println(desensitizeString("138", 3, 3, "^_^", 0));
        System.out.println(desensitizeString("13800", 3, 3, "^_^", 0));
        System.out.println(desensitizeString("13800138000", 3, 3, "^_^", 0));


        long time = System.currentTimeMillis() / 5_000;
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        if (sb.length() < 16) {
            sb.append("0".repeat(Math.max(0, 16 - sb.length())));
        }
        System.out.println(sb.toString());

    }

    private String desensitizeString(String value, int prefixLen, int suffixLen, String replacement, int replacementLen) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (replacement == null || replacement.isEmpty()) {
            replacement = "*";
        }
        int len = prefixLen + suffixLen;
        if (value.length() <= prefixLen) {
            // 不足前缀长度，则整体处理
            int repeatLen = Math.ceilDiv(value.length(), replacement.length());
            return replacement.repeat(repeatLen);
        } else if (value.length() <= len) {
            // 不足前后缀总长度时，保留前缀，后面的处理
            int repeatLen = Math.ceilDiv((value.length() - prefixLen), replacement.length());
            return value.substring(0, prefixLen) + replacement.repeat(repeatLen);
        }
        // 中间位置的处理
        if (replacementLen <= 0) {
            replacementLen = Math.ceilDiv((value.length() - len), replacement.length());
        } else {
            replacementLen = Math.ceilDiv(replacementLen, replacement.length());
        }
        return value.substring(0, prefixLen) + replacement.repeat(replacementLen) + value.substring(value.length() - suffixLen);
    }
}
