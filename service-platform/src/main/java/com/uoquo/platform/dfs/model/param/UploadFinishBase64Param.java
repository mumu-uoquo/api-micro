package com.uoquo.platform.dfs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "入参:小文件上传完成")
public class UploadFinishBase64Param {

    /**
     * 若传了路径，则最终保存到该路径下，否则按对应缓存的路径保存
     */
    @Schema(description = "文件保存的目标路径（包含文件名）")
    private String filePath;

    @Schema(description = "是否压缩")
    private Boolean zipEnable;

    @NotEmpty
    @Schema(description = "临时上传码（分块上传场景必传）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> uploadCodes;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getZipEnable() {
        return zipEnable;
    }

    public void setZipEnable(Boolean zipEnable) {
        this.zipEnable = zipEnable;
    }

    public List<String> getUploadCodes() {
        return uploadCodes;
    }

    public void setUploadCodes(List<String> uploadCodes) {
        this.uploadCodes = uploadCodes;
    }
}
