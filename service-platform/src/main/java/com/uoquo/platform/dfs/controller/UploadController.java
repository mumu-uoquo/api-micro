package com.uoquo.platform.dfs.controller;

import com.uoquo.platform.dfs.model.dto.UploadConfigDto;
import com.uoquo.platform.dfs.model.dto.UploadFileDto;
import com.uoquo.platform.dfs.model.param.UploadConfigParam;
import com.uoquo.platform.dfs.model.param.UploadFileParam;
import com.uoquo.platform.dfs.model.param.UploadFinishChunkParam;
import com.uoquo.platform.dfs.model.param.UploadFinishBase64Param;
import com.uoquo.platform.dfs.service.FileUploadService;
import com.uoquo.utils.FileUtil;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ParamErrorException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Tag(name = "dfs", description = "分布式文件管理")
@Validated
@RestController
@RequestMapping("/v1/file/upload")
public class UploadController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileUploadService fileUploadService;

    @IgnoreAuth(inner = true)
    @Operation(summary = "获取临时上传码", hidden = true)
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public ReturnData<UploadConfigDto> getUploadCode(@RequestBody @Valid UploadConfigParam param) {
        // 1. 参数合理化
        if (StringUtil.isNull(param.getFileMd5())) {
            throw new ParamEmtpyException("fileMd5");
        }
        // 去除路径中的“../”
        if (StringUtil.notNull(param.getFilePath())) {
            String filePath = param.getFilePath();
            filePath = filePath.replaceAll("\\.\\."+File.separator, "");
            param.setFilePath(filePath);
        }
        // 去除文件名中的目录
        if (StringUtil.notNull(param.getFileName())) {
            String fileName = param.getFileName();
            int index = fileName.lastIndexOf(File.separator);
            if (index >= 0) {
                param.setFileName(fileName.substring(index + 1));
            }
        }
        // 2. 获取上传配置信息
        UploadConfigDto dto = fileUploadService.getConfig(param);
        return new ReturnData<>(dto);
    }

    @IgnoreAuth(all = true)
    @Operation(summary = "文件上传：分块传输（不支持多线程）", operationId = "uploadByChunk", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "当前块数据",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            parameters = {
                @Parameter(
                        name = "uploadCode", description = "上传码",  required = true,
                        schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string")),
                @Parameter(
                        name = "chunkIndex", description = "当前块序号（从0开始）", required = true,
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "long")),
                @Parameter(name = "chunkSize",  description = "当前块大小（byte）", required = true,
                        schema = @io.swagger.v3.oas.annotations.media.Schema(type = "long"))
            }
    )
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public ReturnData<Integer> uploadByChunk(HttpServletRequest request,
                                             @RequestParam("uploadCode") String uploadCode,
                                             @RequestParam("chunkIndex") Long chunkIndex,
                                             @RequestParam("chunkSize")  Long chunkSize
    ) throws IOException {
        // 1. 参数校验
//        String uploadCode = WebUtil.getValueString(request, "uploadCode", null);
//        Long   chunkIndex = WebUtil.getValueLong(request, "chunkIndex", null);;
//        Long   chunkSize  = WebUtil.getValueLong(request, "chunkSize", null);
        if (StringUtil.isNull(uploadCode)) {
            throw new ParamEmtpyException("uploadCode");
        }
        if (chunkIndex == null) {
            throw new ParamEmtpyException("chunkIndex");
        } else if (chunkIndex < 0) {
            throw new ParamErrorException("chunkIndex");
        }
        if (chunkSize == null) {
            throw new ParamEmtpyException("chunkSize");
        } else if (chunkSize <= 0) {
            throw new ParamErrorException("chunkSize");
        }
        // 2. 文件上传
        Integer nextIndex = fileUploadService.uploadFileByChunk(uploadCode, chunkIndex, chunkSize, request.getInputStream());
        return new ReturnData<>(nextIndex);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "上传完成：分块传输", hidden = true)
    @RequestMapping(value = "/finish/chunk", method = RequestMethod.POST)
    public ReturnData<UploadFileDto> finishByChunk(@RequestBody @Valid UploadFinishChunkParam param) {
        // 1. 参数合理化
        if (StringUtil.isNull(param.getUploadCode())) {
            throw new ParamEmtpyException("uploadCode");
        }
        // 去除路径中的“../”
        if (StringUtil.notNull(param.getFilePath())) {
            String filePath = param.getFilePath();
            filePath = filePath.replaceAll("\\.\\."+File.separator, "");
            param.setFilePath(filePath);
        }
        // 2. 文件转存
        UploadFileDto dto = fileUploadService.finishByChunk(param.getFilePath(), param.getUploadCode());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "文件上传：小文件（BASE64）", operationId = "uploadByBase64", method = "POST")
    @RequestMapping(value = "/base64", method = RequestMethod.POST)
    public ReturnData<UploadFileDto> uploadByBase64(@RequestBody @Valid UploadFileParam param) {
        // 1. 参数合理化
        if (StringUtil.isNull(param.getFileContent())) {
            // 需要文件名的后缀，所以文件名必须传
            throw new ParamEmtpyException("文件内容不可以为空");
        }
        if (StringUtil.isNull(param.getFileName())) {
            // 从内容中提取后缀名
            String suffix = FileUtil.getSuffixByBase64(param.getFileContent());
            if (StringUtil.isNull(suffix)) {
                // 需要文件名的后缀，所以文件名必须传
                throw new ParamEmtpyException("文件名称不可以为空");
            }
            param.setFileName(IDGenerator.getNextULID() + "." + suffix);
        } else {
            // 去除文件名中的目录
            String fileName = param.getFileName();
            int index = fileName.lastIndexOf(File.separator);
            if (index >= 0) {
                param.setFileName(fileName.substring(index + 1));
            }
        }
        if (StringUtil.notNull(param.getFilePath())) {
            // 去除路径中的“../”
            String filePath = param.getFilePath();
            filePath = filePath.replaceAll("\\.\\."+File.separator, "");
            param.setFilePath(filePath);
        }
        // 2. 文件保存
        UploadFileDto dto = fileUploadService.uploadFileByBase64(param);
        return new ReturnData<>(dto);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "上传完成：小文件", hidden = true)
    @RequestMapping(value = "/finish/base64", method = RequestMethod.POST)
    public ReturnData<List<UploadFileDto>> finishByBase64(@RequestBody @Valid UploadFinishBase64Param param) throws IOException {
        // 1. 参数校验
        if (param.getUploadCodes() == null || param.getUploadCodes().isEmpty()) {
            throw new ParamEmtpyException("uploadCode");
        }
        // 压缩时，必须传保存路径
        if (param.getZipEnable() != null && param.getZipEnable()) {
            if (StringUtil.isNull(param.getFilePath())) {
                throw new ParamEmtpyException("filePath");
            }
        }
        // 2. 上传完成
        List<UploadFileDto> result = fileUploadService.finishByBase64(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "文件清理：清理指定的临时文件", operationId = "clearTempFile", method = "POST")
    @RequestMapping(value = "/clear/temp", method = RequestMethod.POST)
    public ReturnData<String> clearTempFile(@RequestBody @Valid UploadFinishBase64Param param) throws IOException {
        // 1. 参数校验
        if (param.getUploadCodes() == null || param.getUploadCodes().isEmpty()) {
            throw new ParamEmtpyException("uploadCode");
        }
        // 2. 清理临时文件
        fileUploadService.clearTempFile(param.getUploadCodes());
        return new ReturnData<>();
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "文件清理：清理过期的临时文件（定时器调用）", hidden = true)
    @RequestMapping(value = "/clear/expired", method = RequestMethod.POST)
    public ReturnData<String> clearExpiredFile() {
        fileUploadService.clearTempFile();
        return new ReturnData<>();
    }
}
