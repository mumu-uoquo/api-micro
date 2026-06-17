package com.uoquo.platform.user.mapper;

import org.apache.ibatis.annotations.Param;

import com.uoquo.platform.user.model.pojo.UserCredential;

public interface UserCredentialMapper {

    /**
     * 按凭证类型+值+机构查询凭证记录
     * wechat 等全局类型传 instituteId=null
     */
    UserCredential selectByCredentialType(
            @Param("credentialType")  String credentialType,
            @Param("credentialValue") String credentialValue,
            @Param("instituteId")     String instituteId);

    /**
     * 凭证 upsert（ON DUPLICATE KEY UPDATE credential_value）
     * 唯一键：(credential_type, credential_value, institute_id)
     * id 由 Service 层通过 IDGenerator.getNextULID() 生成后传入
     */
    int upsertCredential(
            @Param("id")              String id,
            @Param("userId")          String userId,
            @Param("credentialType")  String credentialType,
            @Param("credentialValue") String credentialValue,
            @Param("instituteId")     String instituteId);
}
