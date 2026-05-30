package com.uoquo.platform.system.model.pojo;

/**
 * Table: sys_area
 */
public class SysArea {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: postal_code
     * Type: CHAR(6)
     * Remark: 邮政编码
     */
    private String postalCode;

    /**
     * Column: geo_code
     * Type: VARCHAR(9)
     * Remark: 地图编码
     */
    private String geoCode;

    /**
     * Column: district_code
     * Type: VARCHAR(9)
     * Remark: 行政编码
     */
    private String districtCode;

    /**
     * Column: district_level
     * Type: INT
     * Remark: 行政级别
     */
    private Integer districtLevel;

    /**
     * Column: phone_code
     * Type: VARCHAR(6)
     * Remark: 长途区号
     */
    private String phoneCode;

    /**
     * Column: province
     * Type: VARCHAR(20)
     * Remark: 省份（全称）
     */
    private String province;

    /**
     * Column: province_short
     * Type: VARCHAR(4)
     * Remark: 省份（短程）
     */
    private String provinceShort;

    /**
     * Column: province_abbr
     * Type: VARCHAR(2)
     * Remark: 省份（简称）
     */
    private String provinceAbbr;

    /**
     * Column: city
     * Type: VARCHAR(20)
     * Remark: 城市
     */
    private String city;

    /**
     * Column: county
     * Type: VARCHAR(20)
     * Remark: 区县
     */
    private String county;

    /**
     * Column: town
     * Type: VARCHAR(100)
     * Remark: 乡镇（街道）
     */
    private String town;

    /**
     * Column: location_lng
     * Type: VARCHAR(50)
     * Remark: 经度
     */
    private String locationLng;

    /**
     * Column: location_lat
     * Type: VARCHAR(50)
     * Remark: 维度
     */
    private String locationLat;

    /**
     * Column: sort_idx
     * Type: INT
     * Default value: 99
     * Remark: 显示顺序
     */
    private Integer sortIdx;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode == null ? null : postalCode.trim();
    }

    public String getGeoCode() {
        return geoCode;
    }

    public void setGeoCode(String geoCode) {
        this.geoCode = geoCode == null ? null : geoCode.trim();
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode == null ? null : districtCode.trim();
    }

    public Integer getDistrictLevel() {
        return districtLevel;
    }

    public void setDistrictLevel(Integer districtLevel) {
        this.districtLevel = districtLevel;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode == null ? null : phoneCode.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getProvinceShort() {
        return provinceShort;
    }

    public void setProvinceShort(String provinceShort) {
        this.provinceShort = provinceShort == null ? null : provinceShort.trim();
    }

    public String getProvinceAbbr() {
        return provinceAbbr;
    }

    public void setProvinceAbbr(String provinceAbbr) {
        this.provinceAbbr = provinceAbbr == null ? null : provinceAbbr.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county == null ? null : county.trim();
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town == null ? null : town.trim();
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

    public Integer getSortIdx() {
        return sortIdx;
    }

    public void setSortIdx(Integer sortIdx) {
        this.sortIdx = sortIdx;
    }
}