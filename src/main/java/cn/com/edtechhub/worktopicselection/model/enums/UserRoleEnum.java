package cn.com.edtechhub.worktopicselection.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 *  
 */
public enum UserRoleEnum {

    USER("student", 0),
    TEACHER("teacher", 1),
    DEPT("dept", 2),
    ADMIN("admin",3),
    ;

    private final String text;

    private final int value;

    UserRoleEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(int value) {
        if (value==-1) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value==value) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

}
