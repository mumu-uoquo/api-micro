package com.uoquo.platform.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import com.uoquo.platform.system.mapper.LicenseRecordMapper;
import com.uoquo.platform.system.model.dto.LicenseRecordDto;
import com.uoquo.platform.system.model.param.LicenseImportParam;
import com.uoquo.platform.system.model.param.LicenseRecordPageParam;
import com.uoquo.platform.system.model.pojo.LicenseRecord;
import com.uoquo.platform.system.service.LicenseService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.crypto.License;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.mybatis.page.PageHelper;

@Service
public class LicenseServiceImpl implements LicenseService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LicenseRecordMapper licenseRecordMapper;

    @Override
    public String getMachineCode() {
        return License.getMachineInfo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLicense(LicenseImportParam param) {
        CurrentUser.UserInfo user = CurrentUser.getInfo();

        LicenseRecord record = new LicenseRecord();
        record.setId(IDGenerator.getNextULID());
        record.setCreateUser(user.getUserId());
        record.setCreateTime(new Date());
        record.setIsCurrent(false);

        try {
            License.save(param.getLicense());
            License.LicenseInfo licenseInfo = License.load();

            record.setSerialNo(License.getMachineInfo());
            record.setLicenseInfo(JsonUtil.serialize(licenseInfo));
            // 激活码由 MyBatis @SensitiveField 拦截器在 insert 时自动 AES 加密
            record.setActivationCode(param.getLicense());

            // 将之前的记录标记为非当前
            licenseRecordMapper.clearAllCurrent();
            record.setIsCurrent(true);
            record.setImportResult(true);

        } catch (Exception e) {
            logger.error("导入 license 失败", e);
            record.setImportResult(false);
            String reason = e.getMessage();
            if (reason != null && reason.length() > 512) {
                reason = reason.substring(0, 512);
            }
            record.setFailReason(reason);
            licenseRecordMapper.insert(record);
            throw new RuntimeException("保存 license 失败：" + e.getMessage(), e);
        }

        licenseRecordMapper.insert(record);
    }

    @Override
    public PageResult<LicenseRecordDto> listRecordByPage(LicenseRecordPageParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        // MyBatis 拦截器对 pojo 中 @SensitiveField 字段自动 AES 解密（activation_code）
        PageList<LicenseRecord> list = (PageList<LicenseRecord>) licenseRecordMapper.selectByPage();
        // pojo → dto 转换
        List<LicenseRecordDto> dtoList = new ArrayList<>();
        for (LicenseRecord record : list.getResult()) {
            LicenseRecordDto dto = new LicenseRecordDto();
            BeanUtils.copyProperties(record, dto);
            dtoList.add(dto);
        }
        return PageResult.of(list, dtoList);
    }
}
