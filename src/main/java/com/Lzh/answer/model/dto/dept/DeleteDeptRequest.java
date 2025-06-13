package com.Lzh.answer.model.dto.dept;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 *  
 */
@Data
public class DeleteDeptRequest implements Serializable {

    /**
     * id
     */
    private String deptName;

    private static final long serialVersionUID = 1L;
}