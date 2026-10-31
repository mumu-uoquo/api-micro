package com.uoquo.platform.system.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import com.uoquo.platform.common.SettingsCode;
import com.uoquo.platform.system.mapper.LicenseRecordMapper;
import com.uoquo.platform.system.model.dto.LicenseInfoDto;
import com.uoquo.platform.system.model.dto.LicenseRecordDto;
import com.uoquo.platform.system.model.param.LicenseImportParam;
import com.uoquo.platform.system.model.param.LicenseInfoParam;
import com.uoquo.platform.system.model.param.LicenseRecordPageParam;
import com.uoquo.platform.system.model.pojo.LicenseRecord;
import com.uoquo.platform.system.service.LicenseService;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.License;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.mybatis.page.PageHelper;

@Service
public class LicenseServiceImpl implements LicenseService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LicenseRecordMapper licenseRecordMapper;

    @Autowired
    private SysSettingService sysSettingService;

    // 手动事务控制
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Override
    public String getMachineCode() {
        // 序列号在系统初始化时生成并存入 sys_setting，之后保持不变；硬件码由 License 自动采集
        String serialNo = sysSettingService.getValueByCode(SettingsCode.SERIAL_NUMBER);
        if (StringUtil.isNull(serialNo)) {
            throw new IllegalStateException("缺少系统配置：" + SettingsCode.SERIAL_NUMBER + "，请重启服务完成初始化");
        }
        return License.getMachineInfo(serialNo);
    }

    @Override
    public void importLicense(LicenseImportParam param) {
        CurrentUser.UserInfo user = CurrentUser.getInfo();

        LicenseRecord record = new LicenseRecord();
        record.setId(IDGenerator.getNextULID());
        record.setCreateUser(user.getUserId());
        record.setCreateTime(new Date());
        record.setIsCurrent(false);

        // 1. 解析授权内容
        try {
            LicenseInfoParam licenseInfo = License.parse(param.getLicense(), LicenseInfoParam.class);
            // 序列号：取授权内容中的序列号（而非本机机器信息密文）
            record.setSerialNo(licenseInfo.getSerialNo());
            // 激活码：取授权内容中的激活码（而非授权密文），由 MyBatis @SensitiveField 拦截器在 insert 时自动 AES 加密
            record.setActivationCode(licenseInfo.getActivationCode());
            // 授权开始日期：颁发时 timestamp 即授权开始时间
            if (licenseInfo.getTimestamp() != null) {
                record.setValidFrom(new Date(licenseInfo.getTimestamp()));
            }
            // 授权结束日期：expiryDate 为 yyyyMMdd，null 表示永久有效
            record.setValidExpire(parseExpiryDate(licenseInfo.getExpiryDate()));
            record.setLicenseInfo(JsonUtil.serialize(licenseInfo));
            record.setIsCurrent(true);
            record.setImportResult(true);
        } catch (Exception e) {
            logger.error("导入 license 失败", e);
            saveFailRecord(record, e);
            throw new RuntimeException("保存 license 失败：" + e.getMessage(), e);
        }

        // 2. 保存授权内容
        // 手动事务：仅覆盖"清除旧的当前标记 + 插入新记录"，二者要么都成功要么都回滚；
        // 失败记录必须在事务回滚后单独写入，不能放在同一事务内，否则会随之回滚而丢失
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            // 将之前的记录标记为非当前
            licenseRecordMapper.clearAllCurrent();
            licenseRecordMapper.insert(record);
            License.save(param.getLicense());
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            if (!txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
            logger.error("导入 license 入库失败", e);
            saveFailRecord(record, e);
            throw new RuntimeException("保存 license 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 写入导入失败记录（不加入任何事务，立即提交）；写入本身失败时仅记录日志，不覆盖原始异常
     */
    private void saveFailRecord(LicenseRecord record, Exception cause) {
        record.setIsCurrent(false);
        record.setImportResult(false);
        String reason = cause.getMessage();
        if (reason != null && reason.length() > 512) {
            reason = reason.substring(0, 512);
        }
        record.setFailReason(reason);
        try {
            licenseRecordMapper.insert(record);
        } catch (Exception ex) {
            logger.error("写入 license 导入失败记录异常", ex);
        }
    }

    /**
     * 将 yyyyMMdd 整数转换为日期（null 表示永久有效）；格式非法时返回 null 并记录日志
     */
    private Date parseExpiryDate(Integer expiryDate) {
        if (expiryDate == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(String.valueOf(expiryDate), DateTimeFormatter.BASIC_ISO_DATE);
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            logger.warn("授权过期日期格式非法: {}", expiryDate);
            return null;
        }
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

    @Override
    public LicenseInfoDto getLicenseInfo() {
        LicenseInfoDto dto = new LicenseInfoDto();
        // 1. 授权文件信息
        LicenseInfoParam licenseInfo = null;
        try {
            licenseInfo = License.load(LicenseInfoParam.class);
        } catch (Exception e) {
            logger.warn("加载许可证信息失败: {}", e.getMessage());
        }
        if (licenseInfo == null) {
            return dto;
        }
        dto.setSerialNo(licenseInfo.getSerialNo());
        dto.setOwner(licenseInfo.getAppName());
        // 2. 最新导入记录
        LicenseRecord record = licenseRecordMapper.selectLatestCurrent();
        if (record == null) {
            return dto;
        }
        // 授权时间：导入时间
        dto.setAuthorizedTime(record.getCreateTime());
        // 过期时间：授权结束日期
        dto.setExpireTime(record.getValidExpire());
        dto.setValidFrom(record.getValidFrom());
        dto.setValidExpire(record.getValidExpire());
        return dto;
    }
}
