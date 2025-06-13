package com.Lzh.answer.model.dto.user;

import cn.hutool.db.Page;
import com.Lzh.answer.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 *  
 */
@Data
public class DeptTeacherQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
}