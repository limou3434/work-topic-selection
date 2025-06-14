package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

@Data
public class SituationVO {
    //总人数
    private int Amount;
    //已选题人数
    private int selectAmount;
    //未选题人数
    private int unselectAmount;
}
