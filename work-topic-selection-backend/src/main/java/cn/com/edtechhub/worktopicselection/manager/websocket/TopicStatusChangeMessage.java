package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选题状态变更消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicStatusChangeMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 题目 ID
     */
    private Long topicId;

    /**
     * 学生账号
     */
    private String studentAccount;

    /**
     * 状态变更信息
     */
    private String message;

    /**
     * 用户信息
     */
    private UserVO userVO;

}
