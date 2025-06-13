package com.Lzh.answer.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class GetStudentByTopicId implements Serializable {
    /**
     * 题目id
     */
    private long id;

    private static final long serialVersionUID = 1L;
}