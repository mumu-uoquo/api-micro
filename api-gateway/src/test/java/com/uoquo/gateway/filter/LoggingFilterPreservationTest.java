/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Preservation 属性测试 — 修复前后均应 PASS。
 *
 * <p>验证普通请求（非 SSE、非 WebSocket）的基线行为：
 * isLongLivedRequest() 对普通请求始终返回 false，对 SSE/WebSocket 请求返回 true。
 * 修复后这些测试必须继续通过，确认无回归。
 *
 * <p>Validates: Requirements 3.1, 3.2, 3.3, 3.4
 */
@DisplayName("Preservation 属性测试（修复前后均预期 PASS）")
class LoggingFilterPreservationTest {

    private boolean invokeIsLongLivedRequest(MockServerHttpRequest request) throws Exception {
        LoggingFilter filter = new LoggingFilter();
        Method method = LoggingFilter.class.getDeclaredMethod("isLongLivedRequest",
                org.springframework.http.server.reactive.ServerHttpRequest.class);
        method.setAccessible(true);
        return (boolean) method.invoke(filter, request);
    }

    @Test
    @DisplayName("Accept: application/json 的请求 → isLongLivedRequest() 应返回 false")
    void preservation1_acceptJson_shouldReturnFalse() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users")
                .header("Accept", "application/json")
                .build();
        assertFalse(invokeIsLongLivedRequest(request));
    }

    @Test
    @DisplayName("无特殊头的 GET 请求 → isLongLivedRequest() 应返回 false")
    void preservation2_plainGet_shouldReturnFalse() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/health")
                .build();
        assertFalse(invokeIsLongLivedRequest(request));
    }

    @Test
    @DisplayName("Content-Type: application/json 的 POST 请求 → isLongLivedRequest() 应返回 false")
    void preservation3_postJson_shouldReturnFalse() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/users")
                .header("Content-Type", "application/json")
                .build();
        assertFalse(invokeIsLongLivedRequest(request));
    }

    @Test
    @DisplayName("Accept: text/event-stream 的请求 → isLongLivedRequest() 应返回 true（对照基线）")
    void preservation4_acceptEventStream_shouldReturnTrue() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/events")
                .header("Accept", "text/event-stream")
                .build();
        assertTrue(invokeIsLongLivedRequest(request));
    }

    @Test
    @DisplayName("路径含 /sse/ 且携带 Accept: text/event-stream → isLongLivedRequest() 应返回 true")
    void preservation5_ssePath_withAcceptEventStream_shouldReturnTrue() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/sse/events")
                .header("Accept", "text/event-stream")
                .build();
        assertTrue(invokeIsLongLivedRequest(request));
    }
}
