/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.handler;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.DateUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.web.ReturnData;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;

/**
 * 全局异常拦截.<br>
 * 参考：{@link org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler}
 * @author: xuhz
 * @date: 2020-06-14 15:32
 */
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.cloud.client.ip-address:${server.address:unknown}}")
    private String serverIp;

    @Value("${server.port}")
    private Integer serverPort;

    private final ErrorProperties errorProperties;

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                  ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, applicationContext);
        this.errorProperties = errorProperties;
    }

    @PostConstruct
    public void init() {
        int idx = serverIp.lastIndexOf(".");
        if (StringUtil.notNull(serverIp) && (idx > -1)) {
            serverIp = serverIp.substring(idx + 1);
        }
    }

    /**
     * 全部按JSON格式输出
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderResponse);
    }

    /**
     * 可以从exchange中拿到当前用户信息，不再从deferContextual中获取
     */
    @Deprecated
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        return Mono.deferContextual(ctx -> {
            CurrentUser.clear();
            if (!ctx.isEmpty()){
                // Reactor 提供了跨线程的上下文传递机制（reactor.util.context.Context）
                // 因为错误信息是异步的，所以需要从 Reactor 的 Context获取信息，并重置MDC
                CurrentUser.setToken(ctx.get(CurrentUser.TOKEN));
                CurrentUser.setNonce(ctx.get(CurrentUser.NONCE));
                CurrentUser.setAppkey(ctx.get(CurrentUser.APPID));
                CurrentUser.setAppVersion(ctx.get(CurrentUser.APP_VERSION));
                CurrentUser.setDeviceId(ctx.get(CurrentUser.DEVICE_ID));
                CurrentUser.setLanguage(ctx.get(CurrentUser.USER_LANGUAGE));
                CurrentUser.setClientIp(ctx.get(CurrentUser.CLIENT_IP));
                CurrentUser.setTraceId(ctx.get(CurrentUser.TRACE_ID));
                CurrentUser.setInfo(ctx.get("current-user"));
                MDC.put("requestId", CurrentUser.getTraceId());
            }
            // 解析具体的异常信息
            return renderResponse( request);
        })
//        .doOnError(throwable -> {
//            log.error("error 2:", throwable);
//        })
        .doFinally(signal -> {
            CurrentUser.clear();
            MDC.remove("requestId");
        });
    }

    /**
     * 异常信息处理
     */
    protected Mono<ServerResponse> renderResponse(ServerRequest request) {
        // 1. 获取当前用户信息（404等报错时，还未进入AuthorizeFilter，所以需要从请求头中获取相关CurrentUser信息）
        GatewayUtil.parseInfo4Attributes(request.exchange().getAttributes());
        if (StringUtil.isNull(CurrentUser.getTraceId())) {
            GatewayUtil.parseInfo4Request(request.exchange());
        }
        // 2. 获取真正的错误信息
        ServerHttpRequest httpReq = request.exchange().getRequest();
        Map<String, Object> map = this.getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        String path = (String) map.get("path");
        Throwable error = this.getError(request);
        if ((error != null) && !(error instanceof AbstractBaseException)) {
            error = (error.getCause() == null) ? error : error.getCause();
        }
        AbstractBaseException ex = null;
        if (error instanceof AppkeyEmptyException
            || error instanceof ParamEmtpyException
            || error instanceof ParamSignEmptyException
        ) {
            ex = (AbstractBaseException) error;
            // 打印“WARN”级别日志
        } else if (error instanceof AbstractBaseException) {
            ex = (AbstractBaseException) error;
            // 打印“ERROR”级别日志
        } else if (error instanceof NotFoundException) {
            // 服务无可用实例
            ex = new UoquoException(SystemReturnCode.SERVICE_UNAVAILABLE, error, ((NotFoundException) error).getReason());
        } else {
            // getOrDefault 当键存在，值为null时也会返回null，而不是默认值
            String message = (String) map.get("message");
            Integer status = (Integer) map.get("status");
            status = (status == null) ? 500 : status;
            if (status == 403) {
                message = StringUtil.notNull(message) ? message : String.format("无权操作[%s]", path);
                ex = new ForbiddenException(message, error);
            } else if (status == 404) {
                message = StringUtil.notNull(message) ? message : String.format("[%s]对应的服务不存在", path);
                ex = new ResourceNotFoundException(message, error);
            } else {
                message = StringUtil.notNull(message) ? message : String.format("[%s]处理出错", path);
                ex = new SystemErrorException(message, error);
            }
            // 打印“ERROR”级别日志
        }
        // 记录堆栈
        if (log.isDebugEnabled()) {
            log.error("request[{}] error trace: ", CurrentUser.getNonce(), ex);
        }
        // 3. 拼装堆栈内容前缀
        Date time = (Date) map.get("timestamp");
        String clientIp  = CurrentUser.getClientIp();
        if (StringUtil.isNull(clientIp)) {
            clientIp = GatewayUtil.getClientIp(httpReq);
            clientIp = StringUtil.isNull(clientIp) ? "unknown" : clientIp;
        }
        // 拼装堆栈内容前缀
        ex.setTraceId(CurrentUser.getTraceId());
        String activeType = System.getProperty("spring.profiles.active");
        if (!"prod".equalsIgnoreCase(activeType)) {
            StringBuffer tracePrefix = new StringBuffer();
            tracePrefix.append("timestamp: ").append(DateUtil.toString(time, DateUtil.FORMAT_TIMESTAMP)).append("\n");
            tracePrefix.append("server: ").append(String.format("%s:%s", serverIp, serverPort)).append("\n");
            tracePrefix.append("client: ").append(clientIp).append("\n");
            tracePrefix.append("from: ").append(path).append("\n");
            tracePrefix.append("traceId: ").append(CurrentUser.getTraceId()).append("\n");
            ex.setTrace(tracePrefix.toString());
        }

        // 4. 读取并缓存请求体（确保后续流程可用），同时打印日志（无论是否读取成功）
        Mono<String> bodyMono = GatewayUtil.readAndCacheBody(request.exchange());
        // 5. 打印日志（使用subscribe确保异步执行，不阻塞主线程）
        AbstractBaseException finalEx = ex;
        String finalClientIp = clientIp;
        String userInfo  = JsonUtil.serialize(CurrentUser.getInfo());
        return bodyMono.doOnNext(body -> {
            log.error("request[{}] [{}] [{}] error. code={}, message={}, server={}:{}, appkey={}, client_ip={}, device={}, token={}, user={}, header={}, cookie={}, params={}, body={}.",
                    CurrentUser.getNonce(), httpReq.getMethod(), path, finalEx.getCode(), finalEx.getMesg(), serverIp, serverPort, CurrentUser.getAppkey(),
                    finalClientIp, CurrentUser.getDeviceId(), CurrentUser.getToken(), userInfo, httpReq.getHeaders(), httpReq.getCookies(), httpReq.getQueryParams(), body);
        }).then(Mono.defer(() -> {
            // 响应输出
            ReturnData<String> data = new ReturnData<>(finalEx);
            return ServerResponse.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("response-code", finalEx.getStatus())
                    .body(BodyInserters.fromValue(data));
        })).doFinally(signal -> {
            MDC.remove("requestId");
        });
    }

    protected ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        if (this.errorProperties.isIncludeException()) {
            options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
        }
        if (isIncludeStackTrace(request, mediaType)) {
            options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        if (isIncludeMessage(request, mediaType)) {
            options = options.including(ErrorAttributeOptions.Include.MESSAGE);
        }
        if (isIncludeBindingErrors(request, mediaType)) {
            options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
        }
        return options;
    }

    /**
     * Determine if the stacktrace attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeStacktrace()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isTraceEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the message attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the message attribute should be included
     */
    protected boolean isIncludeMessage(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isMessageEnabled(request);
            default:
                return false;
        }
    }

    /**
     * Determine if the errors attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the errors attribute should be included
     */
    protected boolean isIncludeBindingErrors(ServerRequest request, MediaType produces) {
        switch (this.errorProperties.getIncludeBindingErrors()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return isBindingErrorsEnabled(request);
            default:
                return false;
        }
    }

}