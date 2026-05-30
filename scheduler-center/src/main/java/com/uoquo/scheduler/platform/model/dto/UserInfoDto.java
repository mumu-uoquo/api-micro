package com.uoquo.scheduler.platform.model.dto;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;
import com.uoquo.mybatis.sensitive.SensitiveData;
import com.uoquo.mybatis.sensitive.SensitiveField;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 出参：用户信息
 */
@SensitiveData // Mybatis 敏感数据处理
@Schema(description = "用户信息")
public class UserInfoDto {

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "用户所属机构id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String instituteId;

    @Schema(description = "用户所属机构名称")
    private String instituteName;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "推介码")
    private String referralCode;

    @Schema(description = "用户账号")
    private String userName;

    @SensitiveField                       // Mybatis 敏感数据处理
    @Sensitive(type = SensitiveType.NAME) // JSON 敏感数据处理
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户编号")
    private String userCode;

    @Schema(description = "三方ID")
    private String thirdId;

    @SensitiveField                        // Mybatis 敏感数据处理
    @Sensitive(type = SensitiveType.PHONE) // JSON 敏感数据处理
    @Schema(description = "电话")
    private String phone;

    @SensitiveField                        // Mybatis 敏感数据处理
    @Sensitive(type = SensitiveType.EMAIL) // JSON 敏感数据处理
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户状态")
    private String status;
    @Schema(description = "用户状态名称")
    private String statusText;

    @Schema(description = "状态变更时间")
    private Date statusTime;

    @Schema(description = "状态变更备注")
    private String statusMemo;

    @Schema(description = "密码强度")
    private String pwdLevel;
    @Schema(description = "密码强度文本")
    private String pwdLevelText;

    @Schema(description = "密码是否过期")
    private Boolean pwdExpired;

    @Schema(description = "密码修改时间")
    private Date pwdEditTime;

    @Schema(description = "最后登录ip")
    private String lastedLoginIp;

    @Schema(description = "连续登录错误次数")
    private Integer loginErrorCount;

    @Schema(description = "最后登录时间")
    private Date lastedLoginTime;

    @Schema(description = "创建时间")
    private Date createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.status = status == null ? null : status.trim();
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getStatusMemo() {
        return statusMemo;
    }

    public void setStatusMemo(String statusMemo) {
        this.statusMemo = statusMemo == null ? null : statusMemo.trim();
    }

    public String getPwdLevel() {
        return pwdLevel;
    }

    public void setPwdLevel(String pwdLevel) {
        this.pwdLevel = pwdLevel;
    }

    public String getPwdLevelText() {
        return pwdLevelText;
    }

    public void setPwdLevelText(String pwdLevelText) {
        this.pwdLevelText = pwdLevelText;
    }

    public Boolean getPwdExpired() {
        return pwdExpired;
    }

    public void setPwdExpired(Boolean pwdExpired) {
        this.pwdExpired = pwdExpired;
    }

    public Date getPwdEditTime() {
        return pwdEditTime;
    }

    public void setPwdEditTime(Date pwdEditTime) {
        this.pwdEditTime = pwdEditTime;
    }

    public String getLastedLoginIp() {
        return lastedLoginIp;
    }

    public void setLastedLoginIp(String lastedLoginIp) {
        this.lastedLoginIp = lastedLoginIp;
    }

    public Integer getLoginErrorCount() {
        return loginErrorCount;
    }

    public void setLoginErrorCount(Integer loginErrorCount) {
        this.loginErrorCount = loginErrorCount;
    }

    public Date getLastedLoginTime() {
        return lastedLoginTime;
    }

    public void setLastedLoginTime(Date lastedLoginTime) {
        this.lastedLoginTime = lastedLoginTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }
}
