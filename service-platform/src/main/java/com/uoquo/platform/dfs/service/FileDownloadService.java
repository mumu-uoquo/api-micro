package com.uoquo.platform.dfs.service;

import com.uoquo.platform.dfs.model.dto.DownloadBase64Dto;
import com.uoquo.platform.dfs.model.dto.DownloadConfigDto;
import com.uoquo.platform.dfs.model.param.DownloadConfigParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 文件下载
 * @author xuhz
 */
public interface FileDownloadService {

	/**
	 * 获取下载码（文件MD5）等信息
	 */
	DownloadConfigDto getConfig(DownloadConfigParam param);

	/**
	 * 分块下载文件
	 * @param downloadCode 下载码
	 * @param response     响应对象
	 * @param startPos     起始位置（可选）
	 * @param endPos       结束为止（可选）
	 */
	void downloadFileByRange(String downloadCode, HttpServletResponse response, Long startPos, Long endPos) throws IOException;

    /**
     * 下载文件（文件流）
     */
    void downloadByStream(DownloadConfigParam param, HttpServletResponse response) throws IOException;

    /**
     * 下载文件（Base64）
     */
    DownloadBase64Dto downloadByBase64(DownloadConfigParam param) throws IOException;

	/**
	 * 分块下载完成（删除下载码）
	 * @param downloadCode 下载码
	 */
	void finished(String downloadCode);
}
