package cn.com.edtechhub.worktopicselection.service;

/**
 * 邮箱服务接口
 * @author ljp
 */
public interface MailService {

    /**
     * 发送验证码邮箱
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendCodeMail(String to, String subject, String content);

}
