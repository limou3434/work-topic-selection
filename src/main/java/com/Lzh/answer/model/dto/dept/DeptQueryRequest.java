package com.Lzh.answer.model.dto.dept;

import com.Lzh.answer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 *  
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeptQueryRequest extends PageRequest implements Serializable {
    private String deptName;
    private static final long serialVersionUID = 1L;
}