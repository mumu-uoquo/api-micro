package com.uoquo.platform.dfs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "入参:下载完成")
public class DownloadFinishParam {

    @Schema(description = "临时下载码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String downloadCode;

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }
}
