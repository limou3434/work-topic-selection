package cn.com.edtechhub.worktopicselection.manager.websocket;

import lombok.Getter;

/**
 * WebSocket消息类型枚举
 */
@Getter
public enum WebSocketMessageTypeEnum {

    /**
     * 信息通知
     */
    INFO("INFO", "信息通知"),

    /**
     * 错误消息
     */
    ERROR("ERROR", "错误消息"),

    /**
     * 选题状态变更
     */
    TOPIC_STATUS_CHANGE("TOPIC_STATUS_CHANGE", "选题状态变更");

    private final String code;
    private final String description;

    WebSocketMessageTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static WebSocketMessageTypeEnum getEnumByCode(String code) {
        for (WebSocketMessageTypeEnum enumItem : WebSocketMessageTypeEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        return null;
    }
}