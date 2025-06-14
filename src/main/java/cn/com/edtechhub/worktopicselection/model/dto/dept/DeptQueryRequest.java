package cn.com.edtechhub.worktopicselection.model.dto.dept;

import cn.com.edtechhub.worktopicselection.common.PageRequest;
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