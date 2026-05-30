package com.uoquo.platform.dfs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "入参:获取上传配置信息")
public class UploadFileParam {

    @Schema(description = "文件相对路径（包含文件名）")
    private String filePath;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "文件内容（Base64）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileContent;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "是否最终文件")
    private Boolean finalFile;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Boolean getFinalFile() {
        return finalFile;
    }

    public void setFinalFile(Boolean finalFile) {
        this.finalFile = finalFile;
    }
}
