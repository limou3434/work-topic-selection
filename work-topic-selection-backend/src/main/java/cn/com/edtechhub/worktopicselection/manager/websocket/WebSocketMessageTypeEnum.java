package cn.com.edtechhub.worktopicselection.manager.websocket;

import lombok.Getter;

/**
 * WebSocket 消息类型枚举
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

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code 编码
     * @param description 描述
     */
    WebSocketMessageTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     */
    public static WebSocketMessageTypeEnum getEnumByCode(String code) {
        for (WebSocketMessageTypeEnum enumItem : WebSocketMessageTypeEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        return null;
    }

}
