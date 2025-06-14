package cn.com.edtechhub.worktopicselection.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 *  
 */
@Data
public class ResetPasswordRequest implements Serializable {

    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 账号
     */
    private String userAccount;


    private static final long serialVersionUID = 1L;
}