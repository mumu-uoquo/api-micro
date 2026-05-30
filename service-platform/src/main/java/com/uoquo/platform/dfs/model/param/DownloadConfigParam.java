package com.uoquo.platform.dfs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "入参:获取下载码")
public class DownloadConfigParam {

    @Schema(description = "文件名称（用于显示）")
    private String fileName;

    @NotBlank
    @Schema(description = "文件相对路径（含文件名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @Schema(description = "业务ID（计算MD5时的盐值）")
    private String businessId;

    @Schema(description = "是否计算MD5（默认不计算）")
    private Boolean calcMd5;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Boolean getCalcMd5() {
        return calcMd5;
    }

    public void setCalcMd5(Boolean calcMd5) {
        this.calcMd5 = calcMd5;
    }
}
