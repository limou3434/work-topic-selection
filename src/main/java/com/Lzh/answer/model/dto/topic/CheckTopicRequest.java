package com.Lzh.answer.model.dto.topic;


import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class CheckTopicRequest implements Serializable {
    private Long id;
    private String status;
    private String reason;
    private static final long serialVersionUID = 1L;
}