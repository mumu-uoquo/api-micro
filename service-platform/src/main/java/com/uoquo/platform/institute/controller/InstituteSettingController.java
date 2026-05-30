package com.uoquo.platform.institute.controller;

import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.platform.institute.service.InstituteSettingService;
import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingCodeParam;
import com.uoquo.platform.system.model.param.SettingSearchParam;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 机构配置控制器
 */
@Tag(name = "institute", description = "机构配置")
@Validated
@RestController
@RequestMapping("/v1/institute/settings")
public class InstituteSettingController {

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Autowired
    private InstituteSettingService instituteSettingService;

    @Operation(summary = "新增或修改机构配置", operationId = "saveInstituteSetting", method = "POST")
    @PostMapping("/save")
    public ReturnData<Void> saveInstituteSetting(@RequestBody @Valid List<SettingSaveParam> param) {
        if (param == null || param.isEmpty()) {
            throw new ParamEmtpyException("请传入机构配置");
        }
        // 获取机构ID
        String currentInstituteId = CurrentUser.getInfo().getInstituteId();
        String instituteId = param.get(0).getInstituteId();
        if (StringUtil.isNull(instituteId)) {
            instituteId = currentInstituteId;
        }
        // 非自己机构的配置，需要权限验证
        if (!currentInstituteId.equals(instituteId)) {
            if (!instituteInfoService.checkSelfManageInstitute(instituteId)) {
                throw new ForbiddenException("只能管理自己管辖的机构");
            }
        }
        instituteSettingService.saveSetting(instituteId, param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除机构配置", operationId = "deleteInstituteSetting", method = "POST")
    @PostMapping("/delete")
    public ReturnData<Void> deleteInstituteSetting(@RequestBody @Valid SettingCodeParam param) {
        // 获取机构ID
        String currentInstituteId = CurrentUser.getInfo().getInstituteId();
        if (StringUtil.isNull(param.getInstituteId())) {
            param.setInstituteId(currentInstituteId);
        }
        // 非自己机构的配置，需要权限验证
        if (!currentInstituteId.equals(param.getInstituteId())) {
            if (!instituteInfoService.checkSelfManageInstitute(param.getInstituteId())) {
                throw new ForbiddenException("只能管理自己管辖的机构");
            }
        }
        
        instituteSettingService.deleteByCode(param.getInstituteId(), param.getConfigCode());
        return new ReturnData<>();
    }

    @Operation(summary = "查询单个机构配置", operationId = "getInstituteSetting", method = "POST")
    @PostMapping("/code")
    public ReturnData<String> getInstituteSetting(@RequestBody @Valid SettingCodeParam param) {
        // 获取机构ID
        String instituteId = param.getInstituteId();
        if (StringUtil.isNull(instituteId)) {
            instituteId = CurrentUser.getInfo().getInstituteId();
        }
        
        String result = instituteSettingService.getValueByCode(instituteId, param.getConfigCode());
        return new ReturnData<>(result);
    }

    @Operation(summary = "查询机构配置列表", operationId = "listInstituteSettings", method = "POST")
    @PostMapping("/list")
    public ReturnData<List<SettingDto>> listInstituteSettings(@RequestBody @Valid SettingSearchParam param) {
        // 获取机构ID
        String instituteId = param.getInstituteId();
        if (StringUtil.isNull(instituteId)) {
            instituteId = CurrentUser.getInfo().getInstituteId();
        }
        
        List<SettingDto> result = instituteSettingService.listByPrefix(instituteId, param.getPrefix());
        return new ReturnData<>(result);
    }
}
