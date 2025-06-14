package cn.com.edtechhub.worktopicselection.model.dto.topic;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class AddCountRequest implements Serializable {

    /**
     * 题目id
     */
    private long id;

    /**
     * 加或者减
     */
    private int count;

    private static final long serialVersionUID = 1L;
}