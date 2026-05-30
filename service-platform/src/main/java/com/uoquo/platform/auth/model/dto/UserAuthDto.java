package com.uoquo.platform.auth.model.dto;

import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserRoleDto;
import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Date;
import java.util.List;

/**
 * 出参：用户认证信息
 */
@Schema(description = "用户认证信息")
public class UserAuthDto {

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "会话token")
    private String accessToken;

    @Schema(description = "刷新token")
    private String refreshToken;

    @Schema(description = "过期时间（秒）")
    private Integer expireTime;

    @Schema(description = "用户所属机构id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String instituteId;

    @Schema(description = "用户所属机构名称")
    private String instituteName;

    @Schema(description = "机构的授权分组（004）")
    private String roleGroup;

    @Schema(description = "推介码")
    private String referralCode;

    @Schema(description = "用户姓名（登录账号）")
    private String userName;

    @Sensitive(type = SensitiveType.NAME)
    @Schema(description = "真实姓名")
    private String realName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "电话")
    private String phone;

    @Sensitive(type = SensitiveType.EMAIL)
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户状态")
    private String status;

    @Schema(description = "双因子状态（disabled-未开启，unbound-未绑定，enabled-已绑定）")
    private String totpStatus;

    @Schema(description = "服务器时间")
    private Date serverTime;

    @Schema(description = "当前角色")
    private String currentRoleId;

    @Schema(description = "角色列表")
    public List<UserRoleDto> roleList;

    @Schema(description = "分组列表")
    public List<GroupDto> groupList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotpStatus() {
        return totpStatus;
    }

    public void setTotpStatus(String totpStatus) {
        this.totpStatus = totpStatus;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public String getCurrentRoleId() {
        return currentRoleId;
    }

    public void setCurrentRoleId(String currentRoleId) {
        this.currentRoleId = currentRoleId;
    }

    public List<UserRoleDto> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<UserRoleDto> roleList) {
        this.roleList = roleList;
    }

    public List<GroupDto> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<GroupDto> groupList) {
        this.groupList = groupList;
    }
}
