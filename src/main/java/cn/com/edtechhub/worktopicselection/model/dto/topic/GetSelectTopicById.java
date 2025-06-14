package cn.com.edtechhub.worktopicselection.model.dto.topic;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class GetSelectTopicById implements Serializable {

    /**
     * 题目id
     */
    private long id;

    private static final long serialVersionUID = 1L;
}