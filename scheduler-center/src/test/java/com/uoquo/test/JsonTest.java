package com.uoquo.test;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.platform.model.dto.UserInfoDto;
import com.uoquo.scheduler.platform.model.pojo.AuthInfo;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.utils.json.TypeToken;
import com.uoquo.web.ReturnData;
import com.uoquo.mybatis.page.PageResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class JsonTest {

    @Test
    public void testJson() {
        String json = "{\"status\":\"00000\",\"code\":\"00000\",\"level\":\"SUCCESS\",\"message\":\"请求成功\",\"data\":{\"pageNum\":1,\"pageSize\":1000,\"size\":4,\"total\":\"4000\",\"pages\":1,\"hasPrevPage\":false,\"hasNextPage\":false,\"result\":[{\"id\":\"34N82P6K7WPRMBRS\",\"instituteId\":\"34E0XA4XT8AFED6H\",\"instituteName\":null,\"deptId\":\"34E0XA4XT8AFEDBB\",\"deptName\":null,\"referralCode\":\"402854\",\"userName\":\"admin\",\"realName\":\"\",\"userCode\":null,\"thirdId\":null,\"phone\":\"138****8000\",\"email\":null,\"avatar\":null,\"status\":\"001001\",\"statusText\":null,\"statusTime\":\"1729395705000\",\"statusMemo\":null,\"pwdLevel\":null,\"pwdLevelText\":null,\"pwdExpired\":false,\"pwdEditTime\":null,\"lastedLoginIp\":\"106.120.39.139\",\"loginErrorCount\":0,\"lastedLoginTime\":\"1762087942000\",\"createTime\":\"1729395705000\",\"userRoleList\":null,\"userGroupList\":null},{\"id\":\"35YSY2T305892ZEY\",\"instituteId\":\"34E0XA4XT8AFED6H\",\"instituteName\":null,\"deptId\":\"34E0XA4XT8AFEDBB\",\"deptName\":null,\"referralCode\":\"092485\",\"userName\":\"a1\",\"realName\":\"张*\",\"userCode\":\"\",\"thirdId\":\"\",\"phone\":\"138****8001\",\"email\":\"\",\"avatar\":null,\"status\":\"001001\",\"statusText\":null,\"statusTime\":\"1751708120000\",\"statusMemo\":null,\"pwdLevel\":\"002009\",\"pwdLevelText\":null,\"pwdExpired\":false,\"pwdEditTime\":null,\"lastedLoginIp\":null,\"loginErrorCount\":0,\"lastedLoginTime\":null,\"createTime\":\"1751708120000\",\"userRoleList\":null,\"userGroupList\":null},{\"id\":\"35YSY3YNWRTX03V2\",\"instituteId\":\"34E0XA4XT8AFED6H\",\"instituteName\":null,\"deptId\":\"34E0XA4XT8AFEDBB\",\"deptName\":null,\"referralCode\":\"002629\",\"userName\":\"a2\",\"realName\":\"\",\"userCode\":\"\",\"thirdId\":\"\",\"phone\":\"138****8002\",\"email\":\"\",\"avatar\":null,\"status\":\"001001\",\"statusText\":null,\"statusTime\":\"1751708139000\",\"statusMemo\":null,\"pwdLevel\":\"002009\",\"pwdLevelText\":null,\"pwdExpired\":false,\"pwdEditTime\":null,\"lastedLoginIp\":null,\"loginErrorCount\":0,\"lastedLoginTime\":null,\"createTime\":\"1751708139000\",\"userRoleList\":null,\"userGroupList\":null},{\"id\":\"35YSY4Y6PAXR4STY\",\"instituteId\":\"34E0XA4XT8AFED6H\",\"instituteName\":null,\"deptId\":\"34E0XA4XT8AFEDBB\",\"deptName\":null,\"referralCode\":\"795829\",\"userName\":\"a3\",\"realName\":\"\",\"userCode\":\"\",\"thirdId\":\"\",\"phone\":\"138****8003\",\"email\":\"\",\"avatar\":null,\"status\":\"001001\",\"statusText\":null,\"statusTime\":\"1751708155000\",\"statusMemo\":null,\"pwdLevel\":\"002009\",\"pwdLevelText\":null,\"pwdExpired\":false,\"pwdEditTime\":null,\"lastedLoginIp\":null,\"loginErrorCount\":0,\"lastedLoginTime\":null,\"createTime\":\"1751708155000\",\"userRoleList\":null,\"userGroupList\":null}]}}";

        Type respType = TypeToken.getParameterized(ReturnData.class, PageResult.class).getType();
        ReturnData<PageResult<UserInfoDto>> resp = JsonUtil.deserialize(json, respType);
        System.out.println(resp);
    }

    @Test
    public void testJson2() {
        // RemoteEvent 的父类有 JsonTypeInfo 的注解，因此字符串中必须有 type 属性
        String json = "{\"type\":\"RemoteEvent\",\"timestamp\":\"1776667853210\",\"originService\":\"service-platform\",\"destinationService\":\"**\",\"id\":\"K7D9N00SM9TXV02Y\",\"retry\":false,\"destination\":null,\"token\":null,\"traceId\":\"K7D9MZHAGVVQYXX7\",\"businessType\":\"USER\",\"businessSubType\":null,\"businessTable\":null,\"businessId\":null,\"businessInstituteId\":null,\"operatorId\":\"34N82P6K7WPRMBRS\",\"operatorName\":null,\"operatorInstituteId\":null,\"operationType\":\"create\",\"operationStatus\":\"success\",\"operationTime\":\"1776667853211\",\"content\":null,\"extension\":{},\"appKey\":\"K4X3Z5W9H6Q0J7Q4\",\"appDeviceId\":null,\"appVersion\":null,\"appIp\":\"127.0.0.1\",\"remarks\":null,\"dataType\":\"com.uoquo.platform.user.model.pojo.UserInfo\",\"oldData\":null,\"newData\":{\"id\":null,\"instituteId\":null,\"deptId\":null,\"referralCode\":null,\"userCode\":null,\"userName\":\"newUserName\",\"realName\":null,\"pinYin\":null,\"phone\":null,\"password\":null,\"email\":null,\"avatar\":null,\"status\":null,\"statusTime\":null,\"statusMemo\":null,\"pwdExpired\":null,\"pwdLevel\":null,\"pwdEditTime\":null,\"loginErrorCount\":null,\"lastedLoginIp\":null,\"lastedLoginTime\":null,\"thirdId\":null,\"createUser\":null,\"createTime\":null,\"updateUser\":null,\"updateTime\":null,\"deleteState\":null}}";
        RemoteEvent<Object> resp = JsonUtil.deserialize(json, RemoteEvent.class);
        System.out.println(resp);

        RemoteEvent<Object> resp2 = JsonUtil.deserialize(json, RemoteEvent.class, AuthInfo.class);
        System.out.println(resp2);
    }
}
