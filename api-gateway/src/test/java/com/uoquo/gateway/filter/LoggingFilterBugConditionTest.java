/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bug Condition 探索性测试 — 修复前运行，预期 FAIL。
 *
 * <p>此测试编码了期望行为，在未修复代码上运行时应失败，失败即证明 bug 存在。
 * 修复后重新运行，预期 PASS。
 *
 * <p>Validates: Requirements 1.1, 1.3
 */
@DisplayName("Bug Condition 探索性测试（修复前预期 FAIL）")
class LoggingFilterBugConditionTest {

    /**
     * 通过反射调用 LoggingFilter 的 private 方法 isSseRequest()。
     */
    private boolean invokeIsLongLivedRequest(MockServerHttpRequest request) throws Exception {
        LoggingFilter filter = new LoggingFilter();
        Method method = LoggingFilter.class.getDeclaredMethod("isLongLivedRequest",
                org.springframework.http.server.reactive.ServerHttpRequest.class);
        method.setAccessible(true);
        return (boolean) method.invoke(filter, request);
    }

    /**
     * Bug Condition 1：路径含 /sse/，无 Accept 头，无 Content-Type 头。
     *
     * <p>期望 isSseRequest() 返回 true（路径约定即为 SSE 请求）。
     * 实际（未修复）：检查 Content-Type，Content-Type 为空，返回 false。
     *
     * <p>反例：path=/sse/events, no Accept, no Content-Type → isSseRequest() = false (expected true)
     */
    @Test
    @DisplayName("路径含 /sse/events，无 Accept 头，无 Content-Type 头 → isSseRequest() 应返回 true（Bug: 实际返回 false）")
    void bugCondition1_ssePath_noAccept_noContentType_shouldReturnTrue() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/sse/events")
                .build();

        boolean result = invokeIsLongLivedRequest(request);

        // 期望 true（路径含 /sse/ 应直接识别为 SSE 请求）
        // 未修复时此断言失败，证明 bug 存在
        assertTrue(result,
                "反例: path=/api/sse/events, no Accept, no Content-Type → isSseRequest() = " + result + " (expected true)");
    }

    /**
     * Bug Condition 2：携带 Upgrade: websocket 头。
     *
     * <p>期望 isSseRequest() 返回 true（WebSocket 升级请求为长连接）。
     * 实际（未修复）：完全未检测 Upgrade 头，返回 false。
     *
     * <p>反例：Upgrade: websocket → isSseRequest() = false (expected true)
     */
    @Test
    @DisplayName("携带 Upgrade: websocket 头 → isSseRequest() 应返回 true（Bug: 实际返回 false）")
    void bugCondition2_upgradeWebSocket_shouldReturnTrue() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/chat/ws")
                .header("Upgrade", "websocket")
                .header("Connection", "Upgrade")
                .build();

        boolean result = invokeIsLongLivedRequest(request);

        // 期望 true（WebSocket 升级请求应被识别为长连接）
        // 未修复时此断言失败，证明 bug 存在
        assertTrue(result,
                "反例: Upgrade: websocket → isSseRequest() = " + result + " (expected true)");
    }
}
