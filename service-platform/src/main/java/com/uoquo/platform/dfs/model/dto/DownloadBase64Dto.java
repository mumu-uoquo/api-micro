package com.uoquo.platform.dfs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "出参:下载文件（Base64）")
public class DownloadBase64Dto {

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件内容（Base64）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileContent;

    @Schema(description = "文件MD5")
    private String fileMd5;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
}
