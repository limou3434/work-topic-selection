package cn.com.edtechhub.worktopicselection.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 选题审核状态枚举
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter
public enum TopicStatusEnum {

    NOT_PUBLISHED(0, "未发布"),

    PUBLISHED(1, "已发布"),

    PENDING_REVIEW(-1, "待审核"),

    REJECTED(-2, "被打回"),
    ;

    /**
     * 码值
     */
    private final int code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 内部构造方法
     */
    TopicStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     */
    public static TopicStatusEnum getEnums(int code) { // TODO: 等待改正放回值为枚举
        if (ObjUtil.isEmpty(code)) {
            return null;
        }
        for (TopicStatusEnum role : TopicStatusEnum.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return null;
    }

}
