package com.uoquo.platform.dfs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "出参:获取下载配置信息")
public class DownloadConfigDto {

    @Schema(description = "临时下载码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String downloadCode;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

}
