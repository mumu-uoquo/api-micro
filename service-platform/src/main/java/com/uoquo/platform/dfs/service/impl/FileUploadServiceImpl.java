package com.uoquo.platform.dfs.service.impl;

import com.uoquo.platform.common.PlatformCacheKey;
import com.uoquo.platform.common.exception.PlatformReturnCode;
import com.uoquo.platform.dfs.model.dto.UploadConfigDto;
import com.uoquo.platform.dfs.model.dto.UploadFileDto;
import com.uoquo.platform.dfs.model.param.UploadConfigParam;
import com.uoquo.platform.dfs.model.param.UploadFileParam;
import com.uoquo.platform.dfs.model.param.UploadFinishBase64Param;
import com.uoquo.platform.dfs.model.pojo.UploadConfig;
import com.uoquo.platform.dfs.service.FileUploadService;
import com.uoquo.utils.*;
import com.uoquo.utils.crypto.MD5;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.utils.spring.RedisUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件上传
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

	private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    private static final int BUF_SIZE = 5 * 1024 * 1024;

	/**
	 * 缓存过期时间（默认5小时）
	 * 18000s = 5 * 60 * 60
	 */
	@Value("${app.timeout.up-code:18000}")
	private Integer CACHE_TIMEOUT;

    /**
     * 静态资源前缀
     */
    @Value("${app.host.static:/}")
    private String staticHost;

	/**
	 * 临时上传目录
	 */
	@Value("${app.temp-dir}")
	private String tempDir;

	/**
	 * 文件存储目录
	 */
	@Value("${app.data-dir}")
	private String dataDir;

	/**
	 * 文件分块大小（默认5M）
	 */
	@Value("${app.dfs.chunk-size:5MB}")
	private DataSize chunkSize;

	@Override
	public UploadConfigDto getConfig(UploadConfigParam param) {
        // 生成上传码
        String code = IDGenerator.getNextULID();
		// 1. 计算分块信息
		long partLen = chunkSize.toBytes();
		long partTotal = (long)Math.ceil((double)param.getFileSize() / partLen);
		List<Integer> list = this.checkTempFile(code, param.getFileSize(), partTotal);
		// 2. 将配置信息放入缓存
        String filePath = this.getFilePath(param.getFilePath(), param.getFileName());
        // 放入缓存
		UploadConfig config = new UploadConfig();
		config.setFileMd5(param.getFileMd5());
		config.setFileSize(param.getFileSize());
        config.setFileType(FileUtil.getSuffixByName(param.getFileName()));
        config.setFileName(param.getFileName());
		config.setFilePath(filePath);
		config.setChunkSize(partLen);
		config.setChunkTotal(partTotal);
		config.setBusinessId(param.getBusinessId());
		RedisUtil.put(PlatformCacheKey.DFS_UPLOAD_PREFIX + code, config, CACHE_TIMEOUT);
		// 3. 返回信息
        UploadConfigDto dto = new UploadConfigDto();
		dto.setChunkList(list);
		dto.setChunkSize(partLen);
		dto.setUploadCode(code);
		log.info("read upload file. {code={}, path={}, md5={}, bid={}}", code, filePath, param.getFileMd5(), param.getBusinessId());
		return dto;
	}

	@Override
	public Integer uploadFileByChunk(String uploadCode, long chunkIndex, long chunkSize, InputStream input) throws IOException {
		// 1. 有效性检测
		UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
		if (config == null) {
			throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_INVALID);
		} else if (config.getChunkTotal() < chunkIndex) {
			throw new ParamErrorException("chunkIndex");
		} else if (config.getChunkSize() < chunkSize) {
			throw new ParamErrorException("chunkSize");
		}
		// 2. 临时文件校验
		Path cfgFile = Path.of(String.format("%s/%s.cfg", this.tempDir, uploadCode));
		Path tmpFile = Path.of(String.format("%s/%s.tmp", this.tempDir, uploadCode));
		if (!Files.exists(tmpFile) || !Files.exists(cfgFile)) {
			// 如果有文件不存在，说明是非法请求
			throw new ForbiddenException();
		}
        // 为了限制多线程上传，导致存储暴涨，因此只能顺序上传，也就是当前写入的位置不能大于临时文件的大小
        long pos = chunkIndex * config.getChunkSize();
		if (Files.size(tmpFile) < pos) {
			throw new ParamErrorException("chunkIndex");
		}
		// 3. 上传当前块数据
		try (
				FileChannel rafTmp = FileChannel.open(tmpFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
				FileChannel rafCfg = FileChannel.open(cfgFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
		) {
			// 3.1 将分块数据写入文件（一次读取1M）
			rafTmp.position(pos);
			long writeLen = 0;
			byte[] bytes = new byte[1024 * 1024];
			int len = -1;
			while((len = input.read(bytes)) != -1) {
				ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, len);
				while (buffer.hasRemaining()) {
					rafTmp.write(buffer);
				}
				writeLen += len;
			}
			// 3.2 更新分块完成信息
			if (writeLen < chunkSize) {
				log.error("upload file chunk error. {code={}, chunk={}, size={}, writeSize={}, config={}}",
						uploadCode, chunkIndex, chunkSize, writeLen, JsonUtil.serialize(config));
				throw new UoquoException(PlatformReturnCode.FILE_SAVE_FAILED);
			}
			// 3.2 更新分块完成信息
			rafCfg.write(ByteBuffer.wrap(new byte[]{Byte.MAX_VALUE}), chunkIndex);
			log.debug("upload file chunk success. {code={}, chunk={}, size={}, config={}}", uploadCode, chunkIndex, chunkSize, JsonUtil.serialize(config));
		} catch (IOException e) {
			log.error("upload file chunk error. {code={}, chunk={}, size={}, config={}}", uploadCode, chunkIndex, chunkSize, JsonUtil.serialize(config), e);
			throw e;
		}
		// 4. 完成校验
		List<Integer> list = this.getUnfinishedChunks(uploadCode, config.getChunkTotal());
		if (!list.isEmpty()) {
			return list.get(0);
		}
        // 5. 校验MD5（仅最后一包数据传完了才会触发）
        if (StringUtil.isNull(config.getFileMd5())) {
            log.debug("file md5 is not cache, skip check md5. {code={}, path={}, bid={}}", uploadCode, config.getFilePath(), config.getBusinessId());
            return null;
        }
		String md5 = MD5.encryptFile(tmpFile.toAbsolutePath().toString(), config.getBusinessId());
		if (!config.getFileMd5().equalsIgnoreCase(md5)) {
            // 调试模式时不删除临时文件，方便对照排查
            if (log.isInfoEnabled()) {
                deleteFile(cfgFile);
                deleteFile(tmpFile);
            }
			RedisUtil.remove(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode);
			log.error("check file md5 error. {code={}, chunk={}, size={}, server_md5={}, config={}}", uploadCode, chunkIndex, chunkSize, md5, JsonUtil.serialize(config));
            throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_BROKEN);
		}
		log.info("check file md5 success. {code={}, chunk={}, size={}, config={}}", uploadCode, chunkIndex, chunkSize, JsonUtil.serialize(config));
		return null;
	}

	@Override
	public UploadFileDto finishByChunk(String filePath, String uploadCode) {
		// 1. 有效性校验
        UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
        RedisUtil.remove(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode);
        if (config == null) {
            throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_INVALID);
        }
        try {
            checkTempFileMd5(uploadCode, config.getBusinessId(), config.getFileMd5());
        } catch (Exception e) {
            log.error("bid[{}] code[{}] check md5[{}] failed: {}", config.getBusinessId(), uploadCode, config.getFileMd5(), e.getMessage());
            throw e;
        }
        // 2. 迁移临时文件到存储路径
        String dstFilePath = StringUtil.notNull(filePath) ? filePath : config.getFilePath();
		Path cfgFile = Path.of(String.format("%s/%s.cfg", this.tempDir, uploadCode));
		Path tmpFile = Path.of(String.format("%s/%s.tmp", this.tempDir, uploadCode));
        Path dstFile = this.resolveDataPath(dstFilePath);
		try {
			moveFile(tmpFile, dstFile);
			log.debug("bid[{}] code[{}] move file [{}] to [{}].", config.getBusinessId(), uploadCode, tmpFile.toAbsolutePath().toString(), dstFile.toAbsolutePath().toString());
            // 调试模式时不删除临时文件，方便对照排查
            if (log.isInfoEnabled()) {
                deleteFile(cfgFile);
                deleteFile(tmpFile);
            }
			log.info("bid[{}] code[{}] move to [{}] success.", config.getBusinessId(), uploadCode, dstFile.toAbsolutePath().toString());
            UploadFileDto dto = new UploadFileDto();
            dto.setUploadCode(uploadCode);
            dto.setFileMd5(config.getFileMd5());
            dto.setFilePath(dstFilePath);
			return dto;
		} catch (Exception e) {
            log.info("bid[{}] code[{}] move to [{}] failed: {}", config.getBusinessId(), uploadCode, dstFile.toAbsolutePath().toString(), e.getMessage());
            throw new UoquoException(PlatformReturnCode.FILE_SAVE_FAILED);
		}
	}

    @Override
    public UploadFileDto uploadFileByBase64(UploadFileParam param) {
        // 没有文件名时，从 data:[^;]+;base64, 提取后缀名
        if (StringUtil.isNull(param.getFilePath()) && StringUtil.isNull(param.getFileName())) {
            String suffix = FileUtil.getSuffixByBase64(param.getFileContent());
            if (StringUtil.isNull(suffix)) {
                throw new ParamEmtpyException("文件名称不可以为空");
            }
            param.setFileName(IDGenerator.getNextULID() + "." + suffix);
        }
        // 1. 解码
        byte[] bytes ;
        try {
            String fileContent = param.getFileContent();
            // 去除WEB格式的类型头
            fileContent = fileContent.replaceAll("^data:(.)*;base64,", "");
            bytes = Base64.getDecoder().decode(fileContent);
        } catch (Exception e) {
            log.error("Base64解码失败：{}, data=\n{}", e.getMessage(), param.getFileContent());
            throw new UoquoException(PlatformReturnCode.FILE_SAVE_FAILED, e);
        }
        // 2. 最多允许上传2倍分块的大小
        long maxSize = chunkSize.toBytes() * 2;
        if (bytes.length > maxSize) {
            log.warn("允许的文件大小：{}, 待保存的大小：{}", maxSize, bytes.length);
            throw new UoquoException(PlatformReturnCode.FILE_TOO_BIG);
        }
        // 3. 保存
        String uploadCode = IDGenerator.getNextULID();
        UploadFileDto dto = new UploadFileDto();
        dto.setUploadCode(uploadCode);
        dto.setFileName(param.getFileName());
        dto.setFileSize((long) bytes.length);
        dto.setFileType(FileUtil.getSuffixByName(param.getFileName()));
        // 文件转存
        Path saveFile;
        String fileName = String.format("%s.%s", uploadCode, dto.getFileType());
        String filePath = this.getFilePath(param.getFilePath(), fileName);
        if ((param.getFinalFile() != null) && param.getFinalFile()) {
            // 最终存储
            saveFile = this.resolveDataPath(filePath);
            dto.setFilePath(filePath);
        } else {
            // 临时存储
            String tempPath = this.getFilePath("/", fileName);
            saveFile = Path.of(this.tempDir + tempPath);
            dto.setFilePath(String.format("/temp%s", tempPath));
        }
        boolean saveFlag = writeFile(saveFile, bytes);
        if (!saveFlag) {
            log.error("文件[{}]保存失败.", dto.getFilePath());
            throw new UoquoException(PlatformReturnCode.FILE_SAVE_FAILED);
        }
        // 4. TODO 计算文件MD5
//        dto.setFileMd5(MD5.encryptFile(saveFile.toAbsolutePath().toString(), param.getBusinessId()));
        // 5. 放入缓存，用于删除或迁移文件
        UploadConfig config = new UploadConfig();
        config.setFilePath(filePath);
        config.setTempPath(dto.getFilePath().replaceAll("^/temp", ""));
        config.setFileName(dto.getFileName());
        config.setFileSize(dto.getFileSize());
        config.setFileType(dto.getFileType());
        config.setBusinessId(param.getBusinessId());
        RedisUtil.put(PlatformCacheKey.DFS_UPLOAD_PREFIX + dto.getUploadCode(), config, CACHE_TIMEOUT);
        // 6. 返回前端时拼接前缀（用于域名动静分离）
        dto.setShowPath(staticHost + dto.getFilePath());
        return dto;
    }

    @Override
    public UploadFileDto finishByBase64(String uploadCode, String filePath) throws IOException {
        // 1. 基本校验
        UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
        if (config == null) {
            // 当前上传码不存在时，尝试判断最终文件是否存在
            Path dstFile = null;
            if (StringUtil.notNull(filePath)) {
                dstFile = this.resolveDataPath(filePath);
            }
            if (dstFile == null || !Files.exists(dstFile) || Files.isDirectory(dstFile)) {
                throw new UoquoException(PlatformReturnCode.FILE_NOT_EXIST);
            }
            // 拼接返回对象
            UploadFileDto dto = new UploadFileDto();
            dto.setUploadCode(uploadCode);
//            dto.setFileMd5(config.getFileMd5());
            dto.setFileName(dstFile.getFileName().toString());
            dto.setFilePath(filePath);
            dto.setFileSize(Files.size(dstFile));
            dto.setFileType(FileUtil.getSuffixByName(dstFile.getFileName().toString()));
            return dto;
        }
        // 2. 迁移文件
        UploadFileDto dto = this.finishByBase64Move(uploadCode);
        // 3. 删除上传码
        RedisUtil.remove(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode);
        return dto;
    }

    @Override
    public List<UploadFileDto> finishByBase64(UploadFinishBase64Param param) throws IOException {
        List<UploadFileDto> result = new ArrayList<>();
        if (param.getZipEnable() != null && param.getZipEnable()) {
            // 1. 需要压缩
            // 压缩到临时文件
            String zipFilePath = IDGenerator.getNextULID() + ".zip";
            Path zipFile = Path.of(this.tempDir + "/" + zipFilePath);
            this.finishByBase64Zip(zipFile, param.getUploadCodes());
            // 迁移到最终文件
            Path dstFile = this.resolveDataPath(param.getFilePath());
            moveFile(zipFile, dstFile);
            UploadFileDto dto = new UploadFileDto();
            dto.setFilePath(param.getFilePath());
            result.add(dto);
        } else {
            // 2. 不需要压缩
            for (String uploadCode : param.getUploadCodes()) {
                result.add(this.finishByBase64Move(uploadCode));
            }
        }
        // 3. 删除缓存
        for (String uploadCode : param.getUploadCodes()) {
            UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
            if (config == null) {
                continue;
            }
            RedisUtil.remove(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode);
            Path tmpFile = Path.of(this.tempDir + "/" + config.getTempPath());
            deleteFile(tmpFile);
        }
        return result;
    }

    @Override
    public List<String> deleteFileByPath(List<String> pathList) {
        List<String> result = new ArrayList<>();
        for (String filePath : pathList) {
            if (StringUtil.isNull(filePath)) {
                continue;
            }
            try {
                Path file = this.resolveDataPath(filePath);
                Files.deleteIfExists(file);
                log.info("删除文件[{}]成功.", filePath);
            } catch (Exception e) {
                log.error("删除文件[{}]失败.", filePath, e);
                result.add(filePath);
            }
        }
        return result;
    }

    @Override
    public void clearTempFile(List<String> uploadCodes) {
        for (String uploadCode : uploadCodes) {
            try  {
                UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
                if (config == null) {
                    log.warn("上传缓存[{}]不存在.", uploadCode);
                    continue;
                }
                Path tmpFile = Path.of(this.tempDir + "/" + config.getTempPath());
                deleteFile(tmpFile);
                RedisUtil.remove(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode);
                log.debug("删除临时文件[{}]成功. {}", uploadCode, tmpFile.toAbsolutePath().toString());
            } catch (Exception e) {
                log.error("删除临时文件[{}]失败: {}", uploadCode, e.getMessage());
            }
        }
    }

    /**
     * 批量压缩
     */
    private void finishByBase64Zip(Path zipFile, List<String> uploadCodes) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))))
        {
            for (String uploadCode : uploadCodes) {
                UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
                if (config == null) {
                    log.warn("上传码[{}]不存在", uploadCode);
                    throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_INVALID, uploadCode);
                }
                // 压缩文件
                zipOut.putNextEntry(new ZipEntry(config.getFilePath()));
                Path tmpFile = Path.of(this.tempDir + "/" + config.getTempPath());
                try (
                    BufferedInputStream buffIn = new BufferedInputStream(Files.newInputStream(tmpFile), BUF_SIZE);
                ) {
                    byte[] buf = new byte[BUF_SIZE];
                    int length = -1;
                    while ((length = buffIn.read(buf)) != -1) {
                        zipOut.write(buf, 0, length);
                    }
                    zipOut.flush();
                }
                zipOut.closeEntry();
                log.info("上传[{}]的文件[{}]放入临时压缩包[{}]的[{}]完成.", uploadCode, tmpFile.toAbsolutePath().toString(), zipFile.toAbsolutePath().toString(), config.getFilePath());
            }
            FileUtil.close(zipOut);
        } catch (Exception e) {
            deleteFile(zipFile);
            throw e;
        }
    }

    /**
     * 单文件迁移
     */
    private UploadFileDto finishByBase64Move(String uploadCode) {
        UploadConfig config = RedisUtil.get(PlatformCacheKey.DFS_UPLOAD_PREFIX + uploadCode, UploadConfig.class);
        if (config == null) {
            log.warn("上传码[{}]不存在", uploadCode);
            throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_INVALID, uploadCode);
        }
        Path tmpFile = Path.of(this.tempDir + "/" + config.getTempPath());
        Path dstFile = this.resolveDataPath(config.getFilePath());
        try {
            if (Files.exists(tmpFile)) {
                // 临时文件存在，则说明可以进行迁移
                moveFile(tmpFile, dstFile);
                log.info("bid[{}] code[{}] move to [{}] success.", config.getBusinessId(), uploadCode, dstFile.toAbsolutePath().toString());
            } else if (!Files.exists(dstFile)) {
                // 临时文件和目标文件都不存在，说明文件有损坏
                throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_INVALID, uploadCode);
            } else {
                // 临时文件不存在，但目标文件存在，则说明已经迁移过了
            }
            UploadFileDto dto = new UploadFileDto();
            dto.setUploadCode(uploadCode);
            dto.setFileMd5(config.getFileMd5());
            dto.setFileName(config.getFileName());
            dto.setFilePath(config.getFilePath());
            dto.setFileSize(config.getFileSize());
            dto.setFileType(config.getFileType());
            return dto;
        } catch (Exception e) {
            log.info("bid[{}] code[{}] move to [{}] failed: {}", config.getBusinessId(), uploadCode, dstFile.toAbsolutePath().toString(), e.getMessage());
            throw new UoquoException(PlatformReturnCode.FILE_SAVE_FAILED);
        }
    }

    @Override
    public void clearTempFile() {
        long maxAge = 7 * 24 * 60 * 60 * 1000L;
        Path tempDir = Path.of(this.tempDir);
        if (!Files.isDirectory(tempDir)) {
            return;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(tempDir)) {
            for (Path file : files) {
                clearTempFile(file, maxAge);
            }
        } catch (IOException e) {
            log.warn("遍历临时目录[{}]失败", tempDir, e);
        }
    }

    /**
     * 删除超过7天的文件，以及空目录
     */
    private void clearTempFile(Path file, long maxAge) {
        try {
            if (Files.isDirectory(file)) {
                boolean empty = true;
                try (DirectoryStream<Path> files = Files.newDirectoryStream(file)) {
                    for (Path child : files) {
                        empty = false;
                        clearTempFile(child, maxAge);
                    }
                }
                if (empty) {
                    Files.deleteIfExists(file);
                }
            } else {
                long lastModified = Files.getLastModifiedTime(file).toMillis();
                if (lastModified < System.currentTimeMillis() - maxAge) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            log.warn("清理临时文件[{}]失败", file, e);
        }
    }

	/**
	 * 校验临时文件的有效性
	 */
	private void checkTempFileMd5(String tempFileName, String businessId, String fileMd5) {
		// 1. 有效性校验
		Path cfgFile = Path.of(String.format("%s/%s.cfg", this.tempDir, tempFileName));
		Path tmpFile = Path.of(String.format("%s/%s.tmp", this.tempDir, tempFileName));
		if (!Files.exists(tmpFile) || !Files.exists(cfgFile)) {
            throw new UoquoException(PlatformReturnCode.FILE_NOT_EXIST);
		}
		// 2. 校验MD5（防止没有上传完，直接调用完成接口）
        if (StringUtil.isNull(fileMd5)) {
            log.warn("bid[{}] code[{}] not cache md5, skip check.", businessId, tempFileName);
            return;
        }
        String md5 = MD5.encryptFile(tmpFile.toAbsolutePath().toString(), businessId);
        if (!fileMd5.equalsIgnoreCase(md5)) {
            // 调试模式时不删除临时文件，方便对照排查
            if (log.isInfoEnabled()) {
                deleteFile(cfgFile);
                deleteFile(tmpFile);
            }
            log.warn("bid[{}] code[{}] check md5 failed: cache_md5={}, server_md5={}", businessId, tempFileName, fileMd5, md5);
            throw new UoquoException(PlatformReturnCode.FILE_UPLOAD_BROKEN);
        }
        log.info("bid[{}] code[{}] check md5 success.", businessId, tempFileName);
	}

	/**
	 * 检查临时文件.
	 * <ul>
	 *   <li>1. 文件不存在，创建空文件.</li>
	 *   <li>2. 文件存在，返回未完成的块.</li>
	 * </ul>
	 */
	private List<Integer> checkTempFile(String fileName, long fileLen, long partTotal) {
		// 1. 临时文件及其分片信息
		Path cfgFile = Path.of(String.format("%s/%s.cfg", this.tempDir, fileName));
		Path tmpFile = Path.of(String.format("%s/%s.tmp", this.tempDir, fileName));
		try {
            Files.createDirectories(cfgFile.getParent());
        } catch (IOException e) {
            log.warn("create directory [{}] error.", cfgFile.getParent(), e);
            return this.getUnfinishedChunks(fileName, partTotal);
        }
		// 如果两个文件都存在，说明之前上传过，直接续传
		// 如果有任一一个不存在，则说明是损坏文件，此时删除，重新创建
		if (Files.exists(cfgFile) && Files.exists(tmpFile)) {
			return this.getUnfinishedChunks(fileName, partTotal);
		}
        if (Files.exists(cfgFile)) {
			deleteFile(cfgFile);
		}
        if (Files.exists(tmpFile)) {
			deleteFile(tmpFile);
		}
		// 2. 创建空白文件
		try (
				FileChannel channel = FileChannel.open(cfgFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		) {
            // 创建空白分片信息文件
			if (partTotal > 0) {
                channel.position(partTotal - 1);
                channel.write(ByteBuffer.wrap(new byte[]{0}));
            }
            // 创建空白临时文件
			Files.createFile(tmpFile);
		} catch (IOException e) {
			log.warn("create blank file [{}] error.", cfgFile.toAbsolutePath().toString(), e);
		}
        // 直接创建空白文件，避免一次性预分配完整文件产生额外磁盘占用
		return this.getUnfinishedChunks(fileName, partTotal);
	}

	/**
	 * 获取未上传的文件块.
	 */
	private List<Integer> getUnfinishedChunks(String fileName, long partTotal) {
		List<Integer> list = new ArrayList<>();
		Path cfgFile = Path.of(String.format("%s/%s.cfg", this.tempDir, fileName));
		try (
				FileChannel channel = FileChannel.open(cfgFile, StandardOpenOption.READ);
		) {
			ByteBuffer status = ByteBuffer.allocate(1);
			for (int i = 0; i < partTotal; i++) {
                status.clear();
                int read = channel.read(status, i);
				if (read != 1 || status.get(0) != Byte.MAX_VALUE) {
					list.add(i);
				}
			}
		} catch (IOException e) {
			log.warn("read file [{}] error.", cfgFile.toAbsolutePath().toString(), e);
		}
		return list;
	}

    private Path resolveDataPath(String filePath) {
        String relativePath = filePath.replace('\\', '/');
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        Path root = Path.of(this.dataDir).toAbsolutePath().normalize();
        Path candidate = root.resolve(relativePath).normalize();
        if (!candidate.startsWith(root)) {
            throw new ParamErrorException("filePath");
        }
        return candidate;
    }

    private void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("删除文件[{}]失败", file, e);
        }
    }

    private void moveFile(Path source, Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean writeFile(Path file, byte[] bytes) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("写入文件[{}]失败", file, e);
            return false;
        }
    }

    /**
     * 获取文件保存路径
     */
    private String getFilePath(String filePath, String fileName) {
        // 若指定了文件路径，则使用指定的路径
        if (StringUtil.notNull(filePath)) {
            if (filePath.endsWith("/")) {
                return filePath + fileName;
            } else {
                return filePath;
            }
        }
        // 按自定义规则拼接文件路径
        return String.format("/%s/%s", DateUtil.toString(new Date(), "yyyy/MMdd"), fileName);
    }
}
