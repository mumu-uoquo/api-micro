package com.uoquo.platform.dfs.service.impl;

import com.uoquo.platform.common.PlatformCacheKey;
import com.uoquo.platform.common.exception.PlatformReturnCode;
import com.uoquo.platform.dfs.model.dto.DownloadBase64Dto;
import com.uoquo.platform.dfs.model.dto.DownloadConfigDto;
import com.uoquo.platform.dfs.model.param.DownloadConfigParam;
import com.uoquo.platform.dfs.service.FileDownloadService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.MD5;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.utils.spring.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

/**
 * 文件下载
 */
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadServiceImpl.class);

    /**
     * 缓存过期时间（默认5小时）
     * 目前调整到24小时(86400)
     * 18000s = 5 * 60 * 60
     */
    @Value("${app.timeout.down-code:18000}")
    private Integer CACHE_TIMEOUT;

    /**
     * 文件MD5缓存时间（60s）
     */
    @Value("${app.timeout.down-md5:60}")
    private Integer FILE_MD5_CACHE_TIMEOUT;

    /**
     * 文件存储目录
     */
    @Value("${app.data-dir}")
    private String dataDir;

    @Override
    public DownloadConfigDto getConfig(DownloadConfigParam param) {
        // 1. 判断文件是否存在
        File file = new File(this.dataDir + File.separator + param.getFilePath());
        if (!file.exists()) {
            throw new UoquoException(PlatformReturnCode.FILE_NOT_EXIST, param.getFilePath());
        }
        String filePath = file.getAbsolutePath();
        // 2. 计算文件MD5
        String md5Cache = null;
        if (param.getCalcMd5() != null && param.getCalcMd5()) {
            md5Cache = this.calcFileMd5(file, param.getBusinessId());
        }
        // 2. 缓存下载码与文件路径
        String code = IDGenerator.getNextULID();
        RedisUtil.put(PlatformCacheKey.DFS_DOWNLOAD_PREFIX + code, filePath, CACHE_TIMEOUT);
        log.info("获取文件【{}】的下载码：{md5={}, code={}, bid={}}", filePath, md5Cache, code, param.getBusinessId());
        // 3. 拼接返回参数
        DownloadConfigDto dto = new DownloadConfigDto();
        dto.setDownloadCode(code);
        dto.setFileMd5(md5Cache);
        dto.setFileName(file.getName());
        dto.setFileSize(file.length());
        return dto;
    }

    @Override
    public void downloadFileByRange(String downloadCode, HttpServletResponse response, Long startPos, Long endPos) throws IOException {
        // 1. 有效性校验
        // 1.1 校验下载码
        String filePath = RedisUtil.get(PlatformCacheKey.DFS_DOWNLOAD_PREFIX + downloadCode, String.class);
        if (StringUtil.isNull(filePath)) {
            throw new UoquoException(PlatformReturnCode.FILE_DOWNLOAD_INVALID);
        }
        // 1.2 校验文件（注：缓存的是文件绝对路径）
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }
        // 1.3 合理化分块范围（默认无分块）
        long fileLength = file.length();
        int responseStatus = HttpServletResponse.SC_OK;
        // 判断是否为分片请求，处理Range: bytes=-xxx的场景
        if (startPos != null || endPos != null) {
            responseStatus = HttpServletResponse.SC_PARTIAL_CONTENT;
            // 处理 Range: bytes=-500 格式（仅指定结束偏移）
            if (startPos == null) {
                startPos = fileLength - endPos;
                endPos = null;
            }
        }
        // 修正起始位置：确保非负
        startPos = (startPos == null || startPos < 0) ? 0L : startPos;
        // 修正结束位置：确保不超过文件长度，且至少为0
        endPos = (endPos == null || endPos >= fileLength) ? (fileLength - 1) : endPos;
        // 防护文件长度为1时endPos=-1的情况
        endPos = Math.max(endPos, 0L);
        // 最终校验：如果起始位置大于结束位置，重置为全量响应
        if (startPos > endPos) {
            throw new ParamErrorException("传入的range信息有误");
        }
        // 3. 输出文件
        this.responseFile(file.getName(), file, startPos, endPos, response, responseStatus);
    }

    @Override
    public void downloadByStream(DownloadConfigParam param, HttpServletResponse response) throws IOException {
        // 1. 判断文件是否存在
        File file = new File(this.dataDir + File.separator + param.getFilePath());
        if (!file.exists()) {
            throw new UoquoException(PlatformReturnCode.FILE_NOT_EXIST, param.getFilePath());
        }
        String fileName = StringUtil.notNull(param.getFileName()) ? param.getFileName() : file.getName();
        // 2. 输出文件
        this.responseFile(fileName, file, 0, file.length(), response, HttpServletResponse.SC_OK);
    }

    @Override
    public DownloadBase64Dto downloadByBase64(DownloadConfigParam param) throws IOException {
        // 1. 判断文件是否存在
        File file = new File(this.dataDir + File.separator + param.getFilePath());
        if (!file.exists()) {
            throw new UoquoException(PlatformReturnCode.FILE_NOT_EXIST, param.getFilePath());
        }
        if (file.length() > 1024 * 1024 * 30) {
            throw new UoquoException(PlatformReturnCode.FILE_TOO_BIG, "文件超过30M，不适用Base64传输");
        }
        // 2. 计算文件MD5
        String md5Cache = null;
        if (param.getCalcMd5() != null && param.getCalcMd5()) {
            md5Cache = this.calcFileMd5(file, param.getBusinessId());
        }
        // 3. 读取文件并转换为Base64
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        Base64.Encoder encoder = Base64.getEncoder();
        String fileContent = encoder.encodeToString(fileBytes);
        // 4. 拼接返回参数
        DownloadBase64Dto dto = new DownloadBase64Dto();
        dto.setFileName(file.getName());
        dto.setFileMd5(md5Cache);
        dto.setFileContent(fileContent);
        return dto;
    }

    @Override
    public void finished(String downloadCode) {
        if (StringUtil.isNull(downloadCode)) {
            return;
        }
        String filePath = RedisUtil.get(PlatformCacheKey.DFS_DOWNLOAD_PREFIX + downloadCode, String.class);
        RedisUtil.remove(PlatformCacheKey.DFS_DOWNLOAD_PREFIX + downloadCode);
        log.info("下载码【{}】对应的文件【{}】下载完成", downloadCode, filePath);
    }

    /**
     * 计算文件的MD5
     */
    private String calcFileMd5(File file, String businessId) {
        String md5Cache = null;
        String filePath = file.getAbsolutePath();
        log.debug("获取文件【{}】缓存的MD5开始", filePath);
        long lastModified = file.lastModified();
        String fileMd5Key = MD5.encrypt(filePath + "-" + lastModified, businessId);
        md5Cache = RedisUtil.get(PlatformCacheKey.DFS_FILE_MD5_PREFIX + fileMd5Key, String.class);
        if (md5Cache != null) {
            log.debug("获取到文件【{}】缓存的MD5：lastModified = {}, businessId = {}, md5 = {}", filePath, lastModified, businessId, md5Cache);
        } else {
            log.debug("重计算文件【{}】缓存的MD5开始", filePath);
            md5Cache = MD5.encryptFile(file.getAbsolutePath(), businessId);
            RedisUtil.put(PlatformCacheKey.DFS_FILE_MD5_PREFIX + fileMd5Key, md5Cache, FILE_MD5_CACHE_TIMEOUT);
            log.info("重计算文件【{}】缓存的MD5：lastModified = {}, businessId = {}, md5 = {}", filePath, lastModified, businessId, md5Cache);
        }
        return md5Cache;
    }

    /**
     * 输出文件流
     */
    private void responseFile(String fileName, File file, long startPos, long endPos, HttpServletResponse response, int responseStatus) throws IOException {
        long size = endPos - startPos + 1;
        // 1. 设置响应头
        // 设置状态和长度
        response.reset();
        response.setStatus(responseStatus);
        response.setContentLengthLong(size);
        // 设置文件类型
        MediaType mediaType = MediaTypeFactory.getMediaType(file.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM);
        response.setContentType(mediaType.toString());
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        // 2. 读取分块文件
        if (responseStatus == HttpServletResponse.SC_PARTIAL_CONTENT) {
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Range", String.format("bytes %d-%d/%d", startPos, endPos, file.length()));
        }
        try (
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                OutputStream outs = new BufferedOutputStream(response.getOutputStream());
        ) {
            raf.seek(startPos);
            byte[] bytes = new byte[1024 * 1024]; // 一次读取1M
            long len = 0;
            int n = -1;
            while ((n = raf.read(bytes)) != -1) {
                if ((len + n) > size) {
                    n = (int)(size - len);
                }
                outs.write(bytes, 0, n);
                if ((len += n) >= size) {
                    break;
                }
            }
            outs.flush();
            log.info("download file [{}] success. {from={}, size={}, total={}}", file.getAbsolutePath(), startPos, size, file.length());
        } catch (IOException e) {
            log.error("download file [{}] error. {from={}, size={}, total={}}.", file.getAbsolutePath(), startPos, size, file.length());
            throw e;
        }
    }

}
