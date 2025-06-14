package com.Lzh.answer.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 已登录用户视图（脱敏）
 *
 * @author Lzh*/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 用户角色：user/admin/ban
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户头像
     */
    private String userAvatar="https://pic4.zhimg.com/v2-2eb598cdd000376bf0f6027a7bb326c7_r.jpg";


    private static final long serialVersionUID = 1L;
}