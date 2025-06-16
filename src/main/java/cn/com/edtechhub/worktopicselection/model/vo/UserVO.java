package cn.com.edtechhub.worktopicselection.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 *
 * @author Lzh
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar = "https://pic4.zhimg.com/v2-2eb598cdd000376bf0f6027a7bb326c7_r.jpg";

    /**
     * 用户角色：user/admin/ban
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;
    private String status;

    private static final long serialVersionUID = 1L;
}