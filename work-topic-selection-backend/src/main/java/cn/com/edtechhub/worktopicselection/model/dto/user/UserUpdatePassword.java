package cn.com.edtechhub.worktopicselection.model.dto.user;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserUpdatePassword implements Serializable {

    private String userAccount;

    private String userPassword;

    private String updatePassword;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
