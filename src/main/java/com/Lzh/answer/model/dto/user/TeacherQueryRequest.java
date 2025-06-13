package com.Lzh.answer.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 *  
 */
@Data
public class TeacherQueryRequest implements Serializable {

    /**
     * 用户角色
     */
    private int userRole;

    private static final long serialVersionUID = 1L;
}