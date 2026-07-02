package com.uoquo.platform.common.utils;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.uoquo.utils.crypto.Base32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;

/**
 * TOTP 认证工具类
 * <ul>
 *  <li>双因子认证：基于 RFC 6238 标准，兼容 Google Authenticator </li>
 *  <li>动态码校验：用于手机验证码、邮箱验证码等场景，可减少redis存储</li>
 * </ul>
 *
 * @author xuhz
 */
public class TotpAuthUtils {

    /** 认证码时间步长（秒） */
    public static final int AUTH_TIME_STEP = 30;
    
    /** 动态码时间步长（秒） */
    public static final int DYNAMIC_TIME_STEP = 300;

    /** 默认动态码位数 */
    private static final int DEFAULT_CODE_DIGITS = 6;

    /** HMAC 算法 */
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /**
     * 生成 TOTP 密钥（Base32 编码，16位 = 80 bits）
     *
     * @return Base32 编码的密钥
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[10]; // 80 bits = 10 bytes = 16 Base32 chars
        random.nextBytes(bytes);
        return Base32.encode(bytes);
    }

    /**
     * 验证 TOTP 认证码（允许时间容错, 默认窗口为1）
     *
     * @param secret Base32 编码的密钥
     * @param code   认证码
     * @return 验证通过返回 true
     */
    public static boolean verifyAuthCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        long currentInterval = Instant.now().getEpochSecond() / AUTH_TIME_STEP;
        return verifyCode(secret, code, currentInterval , 1);
    }

    /**
     * 验证 TOTP 动态码（允许时间容错, 默认窗口为1）
     *
     * @param secret Base32 编码的密钥
     * @param code   动态码
     * @return 验证通过返回 true
     */
    public static boolean verifyDynamicCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        long currentInterval = Instant.now().getEpochSecond() / DYNAMIC_TIME_STEP;
        return verifyCode(secret, code, currentInterval , 1);
    }

    /**
     * 验证 TOTP 动态码（允许时间容错）
     *
     * @param secret Base32 编码的密钥
     * @param code   动态码
     * @param window 时间窗口（默认1，允许前后30秒）
     * @return 验证通过返回 true
     */
    private static boolean verifyCode(String secret, String code, long currentInterval, int window) {
        if (secret == null || code == null) {
            return false;
        }
        // 在窗口范围内验证
        for (int i = -window; i <= window; i++) {
            String generatedCode = generateCode(secret, currentInterval + i);
            if (generatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成 otpauth:// URI（用于二维码）
     *
     * @param secret   Base32 编码的密钥
     * @param account  用户账号（通常是手机号或邮箱）
     * @param issuer   issuer（应用名称）
     * @return otpauth:// URI
     */
    public static String generateOtpAuthUri(String secret, String account, String issuer) {
        String encodedIssuer  = URLEncoder.encode(issuer,  StandardCharsets.UTF_8);
        String encodedAccount = URLEncoder.encode(account, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", encodedIssuer, encodedAccount, secret, encodedIssuer);
    }

    /**
     * 生成二维码图片
     *
     * @param otpAuthUri otpauth URI
     * @return 二维码图片 base64 编码
     **/
    public static String generateQrcode(String otpAuthUri) throws IOException, WriterException {
        HashMap<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);  // 边框模块数

        int size = 200;
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(otpAuthUri, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
    }
    
    /**
     * 生成 TOTP 动态码
     *
     * @param secret Base32 编码的密钥
     * @return 动态码
     */
    public static String generateDynamicCode(String secret) {
        if (secret == null) {
            return null;
        }
        long currentInterval = Instant.now().getEpochSecond() / DYNAMIC_TIME_STEP;
        return generateCode(secret, currentInterval);
    }

    /**
     * 生成指定时间步长的 TOTP 动态码
     */
    private static String generateCode(String secret, long timeInterval) {
        try {
            byte[] key = Base32.decode(secret);
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeInterval & 0xFF);
                timeInterval >>= 8;
            }

            SecretKeySpec signKey = new SecretKeySpec(key, HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int code = binary % (int) Math.pow(10, DEFAULT_CODE_DIGITS);
            return String.format("%0" + DEFAULT_CODE_DIGITS + "d", code);
        } catch (Exception e) {
            throw new RuntimeException("生成 TOTP 动态码失败", e);
        }
    }
}
