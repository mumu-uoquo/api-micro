package com.uoquo.test;

import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.utils.ObjectUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.MD5;
import com.uoquo.utils.spring.CaptchaUtil;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class UserTest {

    @Test
    public void testListSteam() {
        List<String> myList = Arrays.asList("apple", null, "orange", "banana");

        List<String> newList = myList.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.length() > 7)
                .sorted(Comparator.comparing(String::length))
//                .collect(Collectors.toCollection(ArrayList::new));
                .collect(Collectors.toList());

        System.out.println(myList);
        System.out.println(newList);
    }

    @Test
    public void testPassword() {
        String password = "admin@123";
        String pswdmd5 = MD5.encrypt(password);
        System.out.println(pswdmd5);
        System.out.println(UserUtils.hashPassword(pswdmd5));
    }

    @Test
    public void test() {
        long time = System.currentTimeMillis() / 5_000; // 5秒内的时间因子相同
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        if (sb.length() < 16) {
            sb.append("0".repeat(Math.max(0, 16 - sb.length())));
        }
        System.out.println(sb.toString());
    }

    @Test
    public void testStream() {
        String idPaths = "1,2,3,,";
        List<String> instituteList = new ArrayList<>();
        if (StringUtil.notNull(idPaths)) {
            Arrays.stream(idPaths.split(","))
                    .filter(StringUtil::notNull)
                    .forEach(instituteList::add);
        }
        System.out.println(instituteList);
    }

    @Test
    public void testCompare() {
        UserInfo user1 = new UserInfo();
        user1.setId("1");
        user1.setUserName("张三");
        UserInfo user2 = new UserInfo();
        user2.setId("1");
        user2.setUserName("李四");

        List<Map<String, Object>> compare = ObjectUtil.compare(user1, user2);
        System.out.println( compare);
    }

    @Test
    public void testCaptcha() {
        CaptchaUtil captchaUtil = new CaptchaUtil();
        String captchaValue = captchaUtil.getCaptchaValue();
        BufferedImage image = captchaUtil.generateCaptchaImage(captchaValue);
        try {
            String base64Image = captchaUtil.convertToWebBase64( image, "jpeg");
            System.out.println("验证码值：" + captchaValue);
            System.out.println(base64Image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
