package cn.com.edtechhub.worktopicselection.model.dto.project;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 *
 */
@Data
public class DeleteProjectRequest implements Serializable {

    /**
     * id
     */
    private String projectName;

    private static final long serialVersionUID = 1L;
}