package com.uoquo.platform.user.model.pojo;

import com.uoquo.mybatis.sensitive.SensitiveData;
import com.uoquo.mybatis.sensitive.SensitiveField;

import java.util.Date;

/**
 * Table: bko_user
 */
@SensitiveData
public class UserInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: institute_id
     * Type: VARCHAR(32)
     * Remark: 所属企业
     */
    private String instituteId;

    /**
     * Column: dept_id
     * Type: VARCHAR(32)
     * Remark: 所属部门
     */
    private String deptId;

    /**
     * Column: referral_code
     * Type: VARCHAR(10)
     * Remark: 专属码
     */
    private String referralCode;

    /**
     * Column: user_code
     * Type: VARCHAR(20)
     * Remark: 工号
     */
    private String userCode;

    /**
     * Column: user_name
     * Type: VARCHAR(50)
     * Remark: 账户
     */
    private String userName;

    /**
     * Column: real_name
     * Type: VARCHAR(100)
     * Remark: 姓名
     */
    @SensitiveField
    private String realName;

    /**
     * Column: pin_yin
     * Type: VARCHAR(20)
     * Remark: 拼音首字母
     */
    private String pinYin;

    /**
     * Column: phone
     * Type: VARCHAR(100)
     * Remark: 电话
     */
    @SensitiveField
    private String phone;

    /**
     * Column: password
     * Type: VARCHAR(100)
     * Remark: 密码
     */
    private String password;

    /**
     * Column: email
     * Type: VARCHAR(200)
     * Remark: 邮箱
     */
    @SensitiveField
    private String email;

    /**
     * Column: totp_secret
     * Type: VARCHAR(200)
     * Remark: 双因子秘钥
     */
    @SensitiveField
    private String totpSecret;

    /**
     * Column: avatar
     * Type: VARCHAR(100)
     * Remark: 头像
     */
    private String avatar;

    /**
     * Column: status
     * Type: CHAR(6)
     * Remark: 可用状态（001）
     */
    private String status;

    /**
     * Column: status_time
     * Type: DATETIME
     * Remark: 状态时间
     */
    private Date statusTime;

    /**
     * Column: status_memo
     * Type: VARCHAR(100)
     * Remark: 状态备注
     */
    private String statusMemo;

    /**
     * Column: is_pwd_expired
     * Type: BIT
     * Default value: 0
     * Remark: 密码是否过期
     */
    private Boolean pwdExpired;

    /**
     * Column: pwd_level
     * Type: CHAR(6)
     * Remark: 密码强度（002）
     */
    private String pwdLevel;

    /**
     * Column: pwd_edit_time
     * Type: DATETIME
     * Remark: 密码修改时间
     */
    private Date pwdEditTime;

    /**
     * Column: login_error_count
     * Type: INT
     * Default value: 0
     * Remark: 连续登录错误次数
     */
    private Integer loginErrorCount;

    /**
     * Column: lasted_login_ip
     * Type: VARCHAR(50)
     * Remark: 最后登录ip
     */
    private String lastedLoginIp;

    /**
     * Column: lasted_login_time
     * Type: DATETIME
     * Remark: 最后登录时间
     */
    private Date lastedLoginTime;

    /**
     * Column: third_id
     * Type: VARCHAR(50)
     * Remark: 三方ID（数据同步）
     */
    private String thirdId;

    /**
     * Column: create_user
     * Type: VARCHAR(32)
     * Remark: 创建人
     */
    private String createUser;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_user
     * Type: VARCHAR(32)
     * Remark: 更新人
     */
    private String updateUser;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default value: 0
     * Remark: 删除标识
     */
    private Long deleteState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId == null ? null : deptId.trim();
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode == null ? null : referralCode.trim();
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode == null ? null : userCode.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPinYin() {
        return pinYin;
    }

    public void setPinYin(String pinYin) {
        this.pinYin = pinYin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret == null ? null : totpSecret.trim();
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

    public Boolean getPwdExpired() {
        return pwdExpired;
    }

    public void setPwdExpired(Boolean pwdExpired) {
        this.pwdExpired = pwdExpired;
    }

    public String getPwdLevel() {
        return pwdLevel;
    }

    public void setPwdLevel(String pwdLevel) {
        this.pwdLevel = pwdLevel == null ? null : pwdLevel.trim();
    }

    public Date getPwdEditTime() {
        return pwdEditTime;
    }

    public void setPwdEditTime(Date pwdEditTime) {
        this.pwdEditTime = pwdEditTime;
    }

    public Integer getLoginErrorCount() {
        return loginErrorCount;
    }

    public void setLoginErrorCount(Integer loginErrorCount) {
        this.loginErrorCount = loginErrorCount;
    }

    public String getLastedLoginIp() {
        return lastedLoginIp;
    }

    public void setLastedLoginIp(String lastedLoginIp) {
        this.lastedLoginIp = lastedLoginIp == null ? null : lastedLoginIp.trim();
    }

    public Date getLastedLoginTime() {
        return lastedLoginTime;
    }

    public void setLastedLoginTime(Date lastedLoginTime) {
        this.lastedLoginTime = lastedLoginTime;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId == null ? null : thirdId.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}