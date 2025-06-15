package cn.com.edtechhub.worktopicselection.model.dto.project;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProjectQueryRequest extends PageRequest implements Serializable {
    private String projectName;
    private String deptName;
    private static final long serialVersionUID = 1L;

}
