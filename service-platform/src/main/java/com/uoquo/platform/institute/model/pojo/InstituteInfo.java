package com.uoquo.platform.institute.model.pojo;

import java.util.Date;

/**
 * Table: inc_institute
 */
public class InstituteInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: parent_id
     * Type: VARCHAR(32)
     * Remark: 父级ID
     */
    private String parentId;

    /**
     * Column: parent_path
     * Type: VARCHAR(256)
     * Remark: 父级ID集合
     */
    private String parentPath;

    /**
     * Column: institute_type
     * Type: CHAR(6)
     * Remark: 企业类型（020）
     */
    private String instituteType;

    /**
     * Column: institute_name
     * Type: VARCHAR(50)
     * Remark: 企业名称
     */
    private String instituteName;

    /**
     * Column: short_name
     * Type: VARCHAR(50)
     * Remark: 企业简称
     */
    private String shortName;

    /**
     * Column: pin_yin
     * Type: VARCHAR(20)
     * Remark: 拼音首字母
     */
    private String pinYin;

    /**
     * Column: institute_code
     * Type: VARCHAR(50)
     * Remark: 企业编码
     */
    private String instituteCode;

    /**
     * Column: district_code
     * Type: VARCHAR(9)
     * Remark: 行政编码
     */
    private String districtCode;

    /**
     * Column: address
     * Type: VARCHAR(100)
     * Remark: 企业地址
     */
    private String address;

    /**
     * Column: role_group
     * Type: VARCHAR(32)
     * Remark: 授权分组（004）
     */
    private String roleGroup;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

    /**
     * Column: status
     * Type: CHAR(6)
     * Remark: 企业状态（021）
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
     * Column: location_lng
     * Type: VARCHAR(50)
     * Remark: 经度
     */
    private String locationLng;

    /**
     * Column: location_lat
     * Type: VARCHAR(50)
     * Remark: 纬度
     */
    private String locationLat;

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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath == null ? null : parentPath.trim();
    }

    public String getInstituteType() {
        return instituteType;
    }

    public void setInstituteType(String instituteType) {
        this.instituteType = instituteType == null ? null : instituteType.trim();
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName == null ? null : instituteName.trim();
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName == null ? null : shortName.trim();
    }

    public String getPinYin() {
        return pinYin;
    }

    public void setPinYin(String pinYin) {
        this.pinYin = pinYin;
    }

    public String getInstituteCode() {
        return instituteCode;
    }

    public void setInstituteCode(String instituteCode) {
        this.instituteCode = instituteCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode == null ? null : districtCode.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup == null ? null : roleGroup.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
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

    public String getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(String locationLng) {
        this.locationLng = locationLng == null ? null : locationLng.trim();
    }

    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat == null ? null : locationLat.trim();
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