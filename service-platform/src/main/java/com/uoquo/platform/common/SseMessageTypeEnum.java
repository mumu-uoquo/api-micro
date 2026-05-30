package com.uoquo.platform.common;

/**
 * 下发SSE的消息类型
 * <ul>
 *     <li>WARNING：异常警告，如被踢下线等</li>
 *     <li>MESSAGE：业务消息（020），如通知公告、系统消息、代办任务</li>
 *     <li>HEARTBEAT：心跳包，用于保持连接</li>
 * </ul>
 * @author xuhz
 */
public enum SseMessageTypeEnum {
    // 未知
    UNKNOWN,
    // 异常警告：如被踢下线等
    WARNING,
    // 业务消息（020）：通知公告、系统消息、代办任务
    MESSAGE,
    // 心跳包：用于保持连接
    HEARTBEAT,
    ;

    public static SseMessageTypeEnum fromName(String code) {
        for (SseMessageTypeEnum item : SseMessageTypeEnum.values()) {
            if (item.name().equals(code)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}
