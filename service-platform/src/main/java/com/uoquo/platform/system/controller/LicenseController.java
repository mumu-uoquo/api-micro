package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.param.LicenseImportParam;
import com.uoquo.platform.system.service.LicenseService;
import com.uoquo.web.ReturnData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "system", description = "授权管理")
@Validated
@RestController
@RequestMapping("/v1/system/license")
public class LicenseController {

    @Resource
    private LicenseService licenseService;

    @Operation(summary = "获取机器码", operationId = "getMachineCode", method = "POST")
    @PostMapping("/machine/code")
    public ReturnData<String> getMachineCode() {
        return new ReturnData<>(licenseService.getMachineCode());
    }

    @Operation(summary = "导入license", operationId = "importLicense", method = "POST")
    @PostMapping("/import")
    public ReturnData<Void> importLicense(@RequestBody @Valid LicenseImportParam param) {
        licenseService.importLicense(param);
        return new ReturnData<>();
    }
}
