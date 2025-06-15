package cn.com.edtechhub.worktopicselection.model.dto.user;

import java.io.Serializable;

import com.sun.xml.internal.ws.developer.Serialization;
import lombok.Data;

/**
 * 用户添加请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 姓名
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
     * 专业
     */
    private String project;

    /**
     * 角色
     */
    private Integer userRole;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}