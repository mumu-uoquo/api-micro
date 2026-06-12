package com.uoquo.test;

import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.AES;
import com.uoquo.utils.crypto.Base32;
import com.uoquo.utils.crypto.RSA;
import com.uoquo.utils.crypto.SM2;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

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

    @Test
    public void testCryptoKey() {
        try {
            String gatewayKey = StringUtil.getRandomString(32);
            System.out.printf("gatewayKey key: len=%d, value=%s%n", gatewayKey.length(), gatewayKey);

            String aesKey = AES.generateKey();
            System.out.printf("aes key: len=%d, value=%s%n", aesKey.length(), aesKey);

            RSA.KeyPair rsaKeyPair = RSA.generateKeyPair();
            String rsaPubKey = rsaKeyPair.getPublicKey();
            String rsaPriKey = rsaKeyPair.getPrivateKey();
            System.out.printf("rsa public  key: len=%d, value=%s%n", rsaPubKey.length(), rsaPubKey);
            System.out.printf("rsa private key: len=%d, value=%s%n", rsaPriKey.length(), rsaPriKey);
            rsaPubKey = AES.encrypt(rsaPubKey, aesKey);
            rsaPriKey = AES.encrypt(rsaPriKey, aesKey);
            System.out.printf("rsa public  key: len=%d, value=%s%n", rsaPubKey.length(), rsaPubKey);
            System.out.printf("rsa private key: len=%d, value=%s%n", rsaPriKey.length(), rsaPriKey);

            SM2.KeyPair sm2KeyPair = SM2.generateKeyPair();
            String sm2PubKey = sm2KeyPair.getPublicKey();
            String sm2PriKey = sm2KeyPair.getPrivateKey();
            System.out.printf("sm2 public  key: len=%d, value=%s%n", sm2PubKey.length(), sm2PubKey);
            System.out.printf("sm2 private key: len=%d, value=%s%n", sm2PriKey.length(), sm2PriKey);
            sm2PubKey = AES.encrypt(sm2PubKey, aesKey);
            sm2PriKey = AES.encrypt(sm2PriKey, aesKey);
            System.out.printf("sm2 public  key: len=%d, value=%s%n", sm2PubKey.length(), sm2PubKey);
            System.out.printf("sm2 private key: len=%d, value=%s%n", sm2PriKey.length(), sm2PriKey);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
