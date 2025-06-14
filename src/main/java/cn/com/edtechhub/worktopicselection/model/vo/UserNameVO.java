package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户Name视图（脱敏）
 *
 *
 * @author Lzh
 */
@Data
public class UserNameVO implements Serializable {



    /**
     * 用户名
     */
    private String userName;


    private static final long serialVersionUID = 1L;
}