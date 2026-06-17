package com.uoquo.platform.user.model.param;

import java.util.List;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 入参：新增用户信息
 */
@Schema(description = "新增用户信息")
public class UserAddParam {

    @Schema(description = "用户所属机构id")
    @NotBlank
    private String instituteId;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "用户编号")
    private String userCode;

    @Schema(description = "三方ID")
    private String thirdId;

    @Schema(description = "用戶密码")
    @NotBlank
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String password;

    @Schema(description = "密码强度")
    private String pwdLevel;

    @Schema(description = "用户账号")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "用户账号只能包含字母、数字、下划线、中横线，且必须以字母开头")
    private String userName;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "电话")
    @NotBlank
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户角色列表")
    public List<String> userRoleIdList;

    @Schema(description = "用户分组列表")
    public List<String> userGroupIdList;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<String> getUserRoleIdList() {
        return userRoleIdList;
    }

    public void setUserRoleIdList(List<String> userRoleIdList) {
        this.userRoleIdList = userRoleIdList;
    }

    public List<String> getUserGroupIdList() {
        return userGroupIdList;
    }

    public void setUserGroupIdList(List<String> userGroupIdList) {
        this.userGroupIdList = userGroupIdList;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
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

    public String getPwdLevel() {
        return pwdLevel;
    }

    public void setPwdLevel(String pwdLevel) {
        this.pwdLevel = pwdLevel;
    }
}
