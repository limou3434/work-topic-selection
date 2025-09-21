package cn.com.edtechhub.worktopicselection.model.dto.topic;

import lombok.Data;

/**
 * 获取题目审核等级请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class GetTopicReviewLevelRequest {

    /**
     * 题目标题
     */
    private String topicTitle;

    /**
     * 题目内容
     */
    private String topicContent;

}
