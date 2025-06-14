package cn.com.edtechhub.worktopicselection.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册请求体
 *
 *  
 */
@Data
public class UserUpdatePassword implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String updatePassword;
}
