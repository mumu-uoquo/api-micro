package com.uoquo.platform.user.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;


/**
 * 入参：用户列表查询
 */
@Schema(description = "用户列表查询")
public class UserListParam extends PageRequest {

    @Schema(description = "父机构id")
    private String instituteParentId;

    @Schema(description = "机构id")
    private String instituteId;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "起始时间")
    private Date createTimeStart;
    @Schema(description = "结束时间")
    private Date createTimeEnd;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getInstituteParentId() {
        return instituteParentId;
    }

    public void setInstituteParentId(String instituteParentId) {
        this.instituteParentId = instituteParentId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
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

    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }
}
