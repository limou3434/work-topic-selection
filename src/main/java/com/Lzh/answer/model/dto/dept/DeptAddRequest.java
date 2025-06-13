package com.Lzh.answer.model.dto.dept;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 *  
 */
@Data
public class DeptAddRequest implements Serializable {


    /**
     * 系部名
     */
    private String deptName;


    private static final long serialVersionUID = 1L;
}