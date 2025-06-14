package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员请求用户数据
 *
 *  
 */
@Data
public class GetUserListRequest implements Serializable {
    /**
     * 用户角色 0 - 普通用户 1 - 教师 2 - 系部 3 - 管理员
     */
    private Integer userRole;

    private static final long serialVersionUID = 1L;
}