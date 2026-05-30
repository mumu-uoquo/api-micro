package com.uoquo.platform.dfs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "入参:分块上传完成")
public class UploadFinishChunkParam {

    /**
     * 若传了路径，则最终保存到该路径下，否则按对应缓存的路径保存
     */
    @Schema(description = "文件保存的目标路径（包含文件名）")
    private String filePath;

    @NotBlank
    @Schema(description = "临时上传码（分块上传场景必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uploadCode;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadCode() {
        return uploadCode;
    }

    public void setUploadCode(String uploadCode) {
        this.uploadCode = uploadCode;
    }
}
