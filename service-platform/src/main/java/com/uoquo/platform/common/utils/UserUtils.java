package com.uoquo.platform.common.utils;

import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.AES;
import com.uoquo.utils.crypto.OTPUtils;
import com.uoquo.utils.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 用户相关工具类
 */
public class UserUtils {
    private final static Logger logger = LoggerFactory.getLogger(UserUtils.class);

    private static final int[] windows = new int[]{0, -1, 1};

    /**
     * 密码解密<br/>
     * 方案：与前端约定对密码MD5后再用AES进行加密传输
     * 秘钥：按5秒为梯度的时间因子，后置补0凑为16位的字符串
     */
    public static String decryptPassword(String password) {
        if (StringUtil.isNull(password)) {
            return null;
        }
        // 生成时间因子秘钥（5秒内相同）
        long time = System.currentTimeMillis() / 5_000;
        // 解码
        for( int i : windows) {
            try {
                return decryptPassword(time + i, password);
            } catch (GeneralSecurityException e) {
                logger.info("密码[{}]采用时间[{}]窗口[{}]解密失败", password, time, i);
            }
        }
        throw new RuntimeException("密码解密失败");
    }

    private static String decryptPassword(long time, String password) throws GeneralSecurityException {
        // 生成16位的时间因子秘钥
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        if (sb.length() < 16) {
            sb.append("0".repeat(Math.max(0, 16 - sb.length())));
        }
        return AES.decrypt(password, sb.toString());
    }

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
            return null;
        }
    }

}
