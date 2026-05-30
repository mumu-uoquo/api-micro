package com.uoquo.platform.dfs.service;


import com.uoquo.platform.dfs.model.dto.UploadConfigDto;
import com.uoquo.platform.dfs.model.dto.UploadFileDto;
import com.uoquo.platform.dfs.model.param.UploadConfigParam;
import com.uoquo.platform.dfs.model.param.UploadFileParam;
import com.uoquo.platform.dfs.model.param.UploadFinishBase64Param;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件上传
 * @author xuhz
 */
public interface FileUploadService {

	/**
	 * 获取上传码等信息.
	 */
	UploadConfigDto getConfig(UploadConfigParam param);

	/**
	 * 文件保存：分块传输
	 * @param uploadCode 上传码
	 * @param chunkIndex 当前块序号（从0开始）
	 * @param chunkSize  当前块大小
	 * @param input      输入流
	 * @return 下一个未完成的块号（多线程上传时不可以参考该返回值）
	 */
	Integer uploadFileByChunk(String uploadCode, long chunkIndex, long chunkSize, InputStream input) throws IOException;

	/**
	 * 上传完成：分块传输（迁移）
     * @param filePath   保存文件路径（可空）
	 * @param uploadCode 上传码
     * @return 最终文件路径及MD5
	 */
    UploadFileDto finishByChunk(String filePath, String uploadCode);

    /**
     * 保存Base64文件
     */
    UploadFileDto uploadFileByBase64(UploadFileParam param);

    /**
     * 上传完成：小文件（单个）<br>
     * 注：当code不存在时，根据该路径检测文件是否存在，常用于表单提交报错，修复后再次提交的情况
     * @param uploadCode 上传码
     * @param filePath   保存文件路径
     * @return 最终文件路径及MD5
     */
    UploadFileDto finishByBase64(String uploadCode, String filePath) throws IOException;

    /**
     * 上传完成：小文件（批量）
     * @return 最终文件路径及MD5
     */
    List<UploadFileDto> finishByBase64(UploadFinishBase64Param param) throws IOException;

    /**
     * 删除指定文件<br>
     * 注：该方法仅系统内使用
     * @param pathList 待删除文件路径
     * @return 失败的文件
     */
    List<String> deleteFileByPath(List<String> pathList);

    /**
     * 清理临时文件（用于应用临时清理）
     */
    void clearTempFile(List<String> uploadCodes);

    /**
     * 清理临时文件（用于内部定时清理）
     */
    void clearTempFile();
}
