package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

@Data
public class DeptTeacherVO {
    private String teacherName;
    private  int topicAmount;
    private  int selectAmount;
    private int surplusQuantity;
}
