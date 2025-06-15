package cn.com.edtechhub.worktopicselection.model.dto.user;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户更新请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    @JsonSerialize(using = ToStringSerializer.class) // 避免 id 过大前端出错
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像 TODO: 本项目是写死的, 可以优化为允许用户提交自己的个性化头像, 不过这可能需要图床支持
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}
