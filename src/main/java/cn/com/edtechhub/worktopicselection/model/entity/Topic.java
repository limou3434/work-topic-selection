package cn.com.edtechhub.worktopicselection.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 
 * @TableName topic
 */
@TableName(value ="topic")
@Data
public class Topic implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class) // 避免 id 过大前端出错
    private Long id;

    /**
     * 题目
     */
    private String topic;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 对学生要求
     */
    private String requirement;

    /**
     * 指导老师
     */
    private String teacherName;

    /**
     * 系部名
     */
    private String deptName;

    /**
     * 系部主任
     */
    private String deptTeacher;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 剩余数量
     */
    private Integer surplusQuantity;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 发布状态
     */
    private Integer status;

    /**
     * 预选人数
     */
    private Integer selectAmount;

    /**
     * 审核理由
     */
    private String reason;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}