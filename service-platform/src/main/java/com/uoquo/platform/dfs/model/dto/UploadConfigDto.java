package com.uoquo.platform.dfs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "出参:获取上传配置信息")
public class UploadConfigDto {

    @Schema(description = "临时上传码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uploadCode;

    @Schema(description = "分块大小（byte）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chunkSize;

    @Schema(description = "未完成的块列表（从0开始）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> chunkList;

    @Schema(description = "是否重复上传", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isRepeatUpload = false;

    public Boolean getIsRepeatUpload() {
		return isRepeatUpload;
	}

	public void setIsRepeatUpload(Boolean isRepeatUpload) {
		this.isRepeatUpload = isRepeatUpload;
	}

	public String getUploadCode() {
        return uploadCode;
    }

    public void setUploadCode(String uploadCode) {
        this.uploadCode = uploadCode;
    }

    public Long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public List<Integer> getChunkList() {
        return chunkList;
    }

    public void setChunkList(List<Integer> chunkList) {
        this.chunkList = chunkList;
    }
}
