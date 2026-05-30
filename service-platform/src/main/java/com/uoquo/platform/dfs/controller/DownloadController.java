package com.uoquo.platform.dfs.controller;

import com.uoquo.platform.dfs.model.dto.DownloadBase64Dto;
import com.uoquo.platform.dfs.model.dto.DownloadConfigDto;
import com.uoquo.platform.dfs.model.param.DownloadConfigParam;
import com.uoquo.platform.dfs.model.param.DownloadFinishParam;
import com.uoquo.platform.dfs.service.FileDownloadService;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.exception.ParamEmtpyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;


@Tag(name = "dfs", description = "分布式文件管理")
@Validated
@RestController
@RequestMapping("/v1/file/download")
public class DownloadController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileDownloadService fileDownloadService;

    @IgnoreAuth(inner = true)
    @Operation(summary = "获取临时下载码", hidden = true)
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public ReturnData<DownloadConfigDto> getDownloadCode(@RequestBody @Valid DownloadConfigParam param) throws FileNotFoundException {
        DownloadConfigDto dto = fileDownloadService.getConfig(param);
        return new ReturnData<>(dto);
    }

    @IgnoreAuth(all = true)
    @Operation(summary = "文件下载：分块传输", operationId = "downloadFileByRange", method = "GET",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            parameters = {
                    @Parameter(name = "downloadCode", description = "下载码",  required = true),
                    @Parameter(
                            name = "Range",
                            description = "请求的分块字节范围，格式示例：bytes=0-1048575",
                            required = false,
                            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string")
                    )
            }
    )
    @RequestMapping(value = "/transfer", method = RequestMethod.GET)
    public void downloadFileByRange(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam("downloadCode") String downloadCode ) throws IOException {
        // 1. 参数校验
        if (StringUtil.isNull(downloadCode)) {
            throw new ParamEmtpyException("downloadCode");
        }
        // 2. 获取返回的数据范围（暂不支持多段）
        /*
         * Range: bytes=0-499   表示第 0-499 字节范围的内容（共500字节）
         * Range: bytes=500-    表示从第 500 字节开始到文件结尾
         * Range: bytes=-500    表示最后 500 字节的内容
         * Range: bytes=0-0,-1  表示第一个和最后一个字节
         * Range: bytes=500-600,601-999 同时指定几个范围
         */
        // 2.1 解析range
        Long bgnPos = null;
        Long endPos = null;
        String range = request.getHeader("Range");
        if (!StringUtil.isNull(range)) {
            String[] str = range.replaceAll("bytes=", "").split(",")[0].split("-");
            if (!StringUtil.isNull(str[0])) {
                try {
                    bgnPos = Long.parseLong(str[0]);
                } catch (Exception e) {
                    logger.warn("无法解析range[{}]的第一个数值[{}]。", range, str[0]);
                }
            }
            if ((str.length == 2) && StringUtil.notNull(str[1])) {
                try {
                    endPos = Long.parseLong(str[1]);
                } catch (Exception e) {
                    logger.warn("无法解析range[{}]的第二个数值[{}]。", range, str[1]);
                }
            }
        }
        // 4. 输出具体内容
        fileDownloadService.downloadFileByRange(downloadCode, response, bgnPos, endPos);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "下载完成", hidden = true)
    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    public ReturnData<String> downloadFinish(@RequestBody DownloadFinishParam param) {
        fileDownloadService.finished(param.getDownloadCode());
        return new ReturnData<>();
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "文件下载：流式传输", hidden = true)
    @RequestMapping(value = "/stream", method = RequestMethod.POST)
    public void downloadByStream(@RequestBody @Valid DownloadConfigParam param, HttpServletResponse response) throws IOException {
        fileDownloadService.downloadByStream(param, response);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "文件下载：BASE64", hidden = true)
    @RequestMapping(value = "/base64", method = RequestMethod.POST)
    public ReturnData<DownloadBase64Dto> downloadByBase64(@RequestBody @Valid DownloadConfigParam param) throws IOException {
        DownloadBase64Dto dto = fileDownloadService.downloadByBase64(param);
        return new ReturnData<>(dto);
    }
}
