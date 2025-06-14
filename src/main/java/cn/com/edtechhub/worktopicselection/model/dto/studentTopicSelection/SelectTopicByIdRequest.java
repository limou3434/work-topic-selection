package cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName student_topic_selection
 */
@Data
public class SelectTopicByIdRequest implements Serializable {
    /**
     * 题目id
     */
    private Long id;

    /**
     * 1-表示选题，0-表示退选
     */
    private int status;
    private static final long serialVersionUID = 1L;
}