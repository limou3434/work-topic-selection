package com.Lzh.answer.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户创建请求
 *
 *  
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 系部
     */
    private String deptName;

    /**
     * 用户角色 0 - 普通用户 1 - 教师 2 - 系部 3 - 管理员
     */
    private Integer userRole;
    private String project;

    private static final long serialVersionUID = 1L;
}