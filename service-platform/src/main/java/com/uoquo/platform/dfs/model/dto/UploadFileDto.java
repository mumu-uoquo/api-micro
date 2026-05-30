package com.uoquo.platform.dfs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "出参:文件保存结果")
public class UploadFileDto {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "文件名称（上传时的文件名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件相对路径（含文件名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @Schema(description = "文件显示路径（含文件名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String showPath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "临时码")
    private String uploadCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getShowPath() {
        return showPath;
    }

    public void setShowPath(String showPath) {
        this.showPath = showPath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getUploadCode() {
        return uploadCode;
    }

    public void setUploadCode(String uploadCode) {
        this.uploadCode = uploadCode;
    }
}
