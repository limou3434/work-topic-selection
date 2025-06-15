package cn.com.edtechhub.worktopicselection.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter
public enum UserRoleEnum { // 由于效率问题, 这里手动缓存了数据库中的用户权限, 数据库中的用户等级只是用来限制插入和前端查询的

    /**
     * 封号角色枚举实例
     */
    BAN_ROLE(-1, "ban"),

    /**
     * 学生角色枚举实例
     */
    DEFAULT_ROLE(0, "user"),

    /**
     * 教师角色枚举实例
     */
    TEACHER(1, "teacher"),

    /**
     * 主任角色枚举实例
     */
    DEPT(2, "dept"),

    /**
     * 系统角色枚举实例
     */
    ADMIN(3, "admin"),
    ;

    /**
     * 角色码值
     */
    private final int code;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 内部角色构造方法
     */
    UserRoleEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据角色码值获取角色枚举
     */
    public static UserRoleEnum getEnums(int code) { // TODO: 等待改正放回值为枚举
        if (ObjUtil.isEmpty(code)) {
            return null;
        }
        for (UserRoleEnum role : UserRoleEnum.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return null;
    }

}
