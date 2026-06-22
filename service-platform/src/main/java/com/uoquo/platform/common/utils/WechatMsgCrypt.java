package com.uoquo.platform.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 微信消息推送加解密工具。
 *
 * <p>实现微信公众号/服务号消息推送的签名校验与安全模式（AES）消息体解密，
 * 参考：
 * <ul>
 *     <li>https://developers.weixin.qq.com/doc/service/guide/dev/push/</li>
 *     <li>https://developers.weixin.qq.com/doc/service/guide/dev/push/encryption.html</li>
 * </ul>
 *
 * <p>加密算法：AES-256-CBC，密钥 = Base64Decode(EncodingAESKey + "=")（32 字节），
 * IV 取密钥前 16 字节；明文结构为 random(16B) + msgLen(4B 网络字节序) + msg + appid。
 */
public final class WechatMsgCrypt {

    private WechatMsgCrypt() {
    }

    /**
     * URL 验证签名（GET）：sha1(sort(token, timestamp, nonce))。
     */
    public static boolean checkSignature(String token, String timestamp, String nonce, String signature) {
        String calc = sha1(token, timestamp, nonce);
        return calc != null && calc.equals(signature);
    }

    /**
     * 消息体签名（POST 安全模式）：sha1(sort(token, timestamp, nonce, encrypt))。
     */
    public static boolean checkMsgSignature(String token, String timestamp, String nonce, String encrypt, String msgSignature) {
        String calc = sha1(token, timestamp, nonce, encrypt);
        return calc != null && calc.equals(msgSignature);
    }

    /**
     * 解密消息体。
     *
     * @param encodingAesKey 服务器配置中的 EncodingAESKey（43 位）
     * @param encrypted      密文（Base64）
     * @return 解密后的明文 XML 消息
     */
    public static String decrypt(String encodingAesKey, String encrypted) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
            byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            // 去除 PKCS7 补位
            int pad = original[original.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            byte[] bytes = Arrays.copyOfRange(original, 0, original.length - pad);

            // 结构：random(16) + msgLen(4, 网络字节序) + msg + appid
            int msgLen = ((bytes[16] & 0xFF) << 24)
                    | ((bytes[17] & 0xFF) << 16)
                    | ((bytes[18] & 0xFF) << 8)
                    | (bytes[19] & 0xFF);
            return new String(bytes, 20, msgLen, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("微信消息解密失败", e);
        }
    }

    /**
     * 字典序排序后拼接，再做 SHA1，返回十六进制小写串。
     */
    private static String sha1(String... params) {
        try {
            Arrays.sort(params);
            StringBuilder sb = new StringBuilder();
            for (String p : params) {
                sb.append(p);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xFF);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
