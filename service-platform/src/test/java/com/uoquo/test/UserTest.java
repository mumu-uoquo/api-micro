package com.uoquo.test;

import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.common.utils.WechatMsgCrypt;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.ObjectUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.*;
import com.uoquo.utils.spring.CaptchaUtil;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class UserTest {

    @Test
    public void testWx() {
        String token = "yocaly_wx_YdQ8YkDAiLiDDnN6t97A";
        String timestamp = "1782182846";
        String nonce = "1634291692";
        String signature = "ba571b4aa17e897df2db489a26731185fb1f3e5a";
        String echostr = "";


        String[] arr = {timestamp, nonce, token};
        Arrays.sort(arr);
        String str2 = String.join("", arr);
        System.out.println(str2);
        System.out.println(SHA.sha1(str2));
        System.out.println(WechatMsgCrypt.checkSignature(token, timestamp, nonce, signature));
    }

    @Test
    public void testWx2() {
        String token = "AAAAA";
        String timestamp = "1714112445";
        String nonce = "415670741";
        String signature = "6c5c811b55cc85e0e1b54100749188c20beb3f5d";
        String msg_signature = "046e02f8204d34f8ba5fa3b1db94908f3df2e9b3";
        String EncodingAESKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String Encrypt = "+qdx1OKCy+5JPCBFWw70tm0fJGb2Jmeia4FCB7kao+/Q5c/ohsOzQHi8khUOb05JCpj0JB4RvQMkUyus8TPxLKJGQqcvZqzDpVzazhZv6JsXUnnR8XGT740XgXZUXQ7vJVnAG+tE8NUd4yFyjPy7GgiaviNrlCTj+l5kdfMuFUPpRSrfMZuMcp3Fn2Pede2IuQrKEYwKSqFIZoNqJ4M8EajAsjLY2km32IIjdf8YL/P50F7mStwntrA2cPDrM1kb6mOcfBgRtWygb3VIYnSeOBrebufAlr7F9mFUPAJGj04=";

        Map<String, String> map = new TreeMap<>();
        map.put("timestamp", timestamp);
        map.put("nonce", nonce);
        map.put("Encrypt", Encrypt);
        map.put("token", token);

        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> {
            System.out.println(key);
            sb.append(value);
        });
        String str = sb.toString();
        System.out.println(str);
        System.out.println(SHA.sha1(str));

        String[] arr = {timestamp, nonce, Encrypt, token};
        Arrays.sort(arr);
        String str2 = String.join("", arr);
        System.out.println(str2);
        System.out.println(SHA.sha1(str2));
        System.out.println(WechatMsgCrypt.checkMsgSignature(token, timestamp, nonce, Encrypt, msg_signature));

        System.out.println(WechatMsgCrypt.decrypt(EncodingAESKey, Encrypt));
    }

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

    @Test
    public void testAESUserId() {
        // 加密
        try {
            String serial = IDGenerator.getUUID();

            String account = StringUtil.getRandomString(30, 6);
            String stateKey = serial + "|" + account;
            String state = encryptTAES(stateKey);
            System.out.println(stateKey);
            System.out.println(state);
            System.out.println(stateKey.length() + " : "+ state.length());
            String redirectUri = "https://www.uoquo.com/health/api/platform/v1/wechat/ops/mfa";
            String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize"
                    + "?appid=wwe196f9393080512d"
                    + "&redirect_uri=" + this.urlEncode(redirectUri)
                    + "&response_type=code"
                    + "&scope=snsapi_privateinfo"
                    + "&state=" + this.urlEncode(state)
                    + "&agentid=1000003"
                    + "#wechat_redirect";

            String qrCode = TotpAuthUtils.generateQrcode(authUrl);
            System.out.println(qrCode);

            String decoded = decryptTAES(state);
            String[] parts = decoded.split("\\|", 2);
            System.out.println(parts.length);
            System.out.println(parts[0]);
            System.out.println(parts[1]);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encryptTAES(String value) throws GeneralSecurityException {
        long step = System.currentTimeMillis() / 5_000;
        StringBuilder sb = new StringBuilder();
        sb.append(step);
        if (sb.length() < 16) {
            sb.append("0".repeat(16 - sb.length()));
        }
        System.out.println("key:"+ sb.toString());
        return AES.encrypt(value, sb.toString());
    }

    private String decryptTAES(String value) throws GeneralSecurityException {
        long step = System.currentTimeMillis() / 5_000;
        StringBuilder sb = new StringBuilder();
        sb.append(step);
        if (sb.length() < 16) {
            sb.append("0".repeat(16 - sb.length()));
        }
        System.out.println("key:"+ sb.toString());
        return AES.decrypt(value, sb.toString());
    }
}
