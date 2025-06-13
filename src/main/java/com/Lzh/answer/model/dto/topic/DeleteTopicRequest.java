package com.Lzh.answer.model.dto.topic;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class DeleteTopicRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    private static final long serialVersionUID = 1L;
}