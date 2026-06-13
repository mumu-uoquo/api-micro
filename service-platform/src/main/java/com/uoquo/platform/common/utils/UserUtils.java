package com.uoquo.platform.common.utils;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.uoquo.utils.DataUtil;
import com.uoquo.utils.crypto.OTPUtils;

/**
 * 用户相关工具类
 */
public class UserUtils {
    private final static Logger logger = LoggerFactory.getLogger(UserUtils.class);

    /**
     * 生成哈希密码
     */
    public static String hashPassword(String password) {
        // 生成随机盐值
        String salt = BCrypt.gensalt();
        // 生成哈希密码
        return BCrypt.hashpw(password, salt);
    }

    /**
     * 密码校验
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * 生成推荐码<br>
     * 规则：根据s1和s2生成6位数字
     */
    public static String generateReferralCode(String s1, String s2) {
        int digits = 6; // 结果为6位数字
        try {
            // OTP
            byte[] code = s1.getBytes(StandardCharsets.UTF_8);
            byte[] data = s2.getBytes(StandardCharsets.UTF_8);
            byte[] hash = OTPUtils.generateHash(code, data);
            int offset = hash[hash.length - 1] & (hash.length - digits);
            // 取4字节组成int
            byte[] bytes = new byte[4];
            System.arraycopy(hash, offset, bytes, 0, bytes.length);
            int div = (int) Math.pow(10, digits);
            long token = DataUtil.getUnsignedInt(bytes) % div;
            // 格式化输出
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
            numberFormat.setMinimumIntegerDigits(digits);
            numberFormat.setGroupingUsed(false);
            return numberFormat.format(token);
        } catch (Exception e) {
            logger.warn("根据[{}, {}]生成推荐码失败：{}", s1, s2, e.getMessage());
            return null;
        }
    }

    /**
     * 取 token 前16字符.<br>
     * 双 token 机制下会话 token 会定期刷新，但前16字符（{@code getNextULID()} 部分）保持不变，
     * 可唯一标识同一会话，兼容 token 刷新后仍能精准寻址。<br>
     * token 为空时返回空字符串。
     *
     * @param token 原始 token
     * @return 前16字符，token 为空时返回空字符串
     */
    public static String formatToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.length() <= 16 ? token : token.substring(0, 16);
    }
}
