package com.uoquo.platform.message.model.pojo;

import java.util.Date;

/**
 * Table: msg_attachment
 */
public class MsgAttachment {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 附件ID
     */
    private String id;

    /**
     * Column: message_id
     * Type: VARCHAR(32)
     * Remark: 消息ID
     */
    private String messageId;

    /**
     * Column: file_name
     * Type: VARCHAR(100)
     * Remark: 文件名
     */
    private String fileName;

    /**
     * Column: file_path
     * Type: VARCHAR(255)
     * Remark: 文件路径
     */
    private String filePath;

    /**
     * Column: file_size
     * Type: BIGINT
     * Remark: 文件大小（字节）
     */
    private Long fileSize;

    /**
     * Column: file_type
     * Type: VARCHAR(32)
     * Remark: 文件类型
     */
    private String fileType;

    /**
     * Column: download_count
     * Type: INT
     * Default value: 0
     * Remark: 下载次数
     */
    private Integer downloadCount;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId == null ? null : messageId.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
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
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}