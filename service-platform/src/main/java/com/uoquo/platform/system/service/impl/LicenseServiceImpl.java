package com.uoquo.platform.system.service.impl;

import com.uoquo.utils.crypto.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.uoquo.platform.system.model.param.LicenseImportParam;
import com.uoquo.platform.system.service.LicenseService;

@Service
public class LicenseServiceImpl implements LicenseService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getMachineCode() {
        return License.getMachineInfo();
    }

    @Override
    public void importLicense(LicenseImportParam param) {
        try {
            License.save(param.getLicense());
        } catch (Exception e) {
            logger.error("导入 license 失败", e);
            throw new RuntimeException("保存 license 失败：" + e.getMessage(), e);
        }
    }
}
