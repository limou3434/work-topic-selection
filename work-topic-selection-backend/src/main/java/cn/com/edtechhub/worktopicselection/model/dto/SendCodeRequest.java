package cn.com.edtechhub.worktopicselection.model.dto;

import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeRequest {

    /**
     * 用户帐号
     */
    private String userAccount;

}
