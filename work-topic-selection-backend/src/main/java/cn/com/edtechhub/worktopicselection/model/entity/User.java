package cn.com.edtechhub.worktopicselection.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @author ljp
 */
@TableName(value = "user")
@Data
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色
     */
    private Integer userRole;

    /**
     * 系部
     */
    private String dept;

    /**
     * 状态
     */
    private String status;

    /**
     * 课题
     */
    private String project;

    /**
     * 已选课题数量
     */
    private Integer topicAmount;

    /**
     * 验证码发送邮箱
     */
    private String email;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}