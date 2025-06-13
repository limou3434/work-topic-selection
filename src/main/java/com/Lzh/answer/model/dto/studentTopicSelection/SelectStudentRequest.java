package com.Lzh.answer.model.dto.studentTopicSelection;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName student_topic_selection
 */
@Data
public class SelectStudentRequest implements Serializable {
    /**
     * 学生账号
     */
    private String userAccount;

    /**
     * 题目
     */
    private String topic;
    private static final long serialVersionUID = 1L;
}