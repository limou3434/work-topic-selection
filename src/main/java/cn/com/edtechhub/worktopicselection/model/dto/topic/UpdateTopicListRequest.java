package cn.com.edtechhub.worktopicselection.model.dto.topic;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author Lzh
 * @TableName topic
 */
@Data
public class UpdateTopicListRequest  implements Serializable {

    private Long id;

    /**
     * 题目
     */
    private String topic;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 对学生要求
     */
    private String requirement;

    /**
     * 系部名
     */
    private String deptName;

    /**
     * 系部主任
     */
    private String deptTeacher;
    /**
     * 指导老师
     */
    private String teacherName;

    private static final long serialVersionUID = 1L;
}
