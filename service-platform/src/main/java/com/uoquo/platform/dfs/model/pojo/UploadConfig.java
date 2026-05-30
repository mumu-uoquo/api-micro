package com.uoquo.platform.dfs.model.pojo;

import java.io.Serializable;

/**
 * 配置信息
 */
public class UploadConfig implements Serializable {

    private static final long serialVersionUID = -7646684123245263476L;

    /**
     * 文件名称（上传时的文件名）
     */
    private String fileName;

    /**
     * 最终文件路径（包含文件名）
     */
    private String filePath;

    /**
     * 临时文件路径（包含文件名）
     */
    private String tempPath;

    /**
     * 文件大小（byte）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 分块大小（byte）
     */
    private Long chunkSize;

    /**
     * 分块个数
     */
    private Long chunkTotal;

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

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
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

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Long getChunkTotal() {
        return chunkTotal;
    }

    public void setChunkTotal(Long chunkTotal) {
        this.chunkTotal = chunkTotal;
    }
}
