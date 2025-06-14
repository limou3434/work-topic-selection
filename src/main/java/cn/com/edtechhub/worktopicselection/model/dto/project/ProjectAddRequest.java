package cn.com.edtechhub.worktopicselection.model.dto.project;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 *  
 */
@Data
public class ProjectAddRequest implements Serializable {


    /**
     * 专业
     */
    private String projectName;
    private String deptName;

    private static final long serialVersionUID = 1L;
}