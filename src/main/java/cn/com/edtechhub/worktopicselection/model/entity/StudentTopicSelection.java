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
 * 学生选题
 * @TableName student_topic_selection
 */
@TableName(value ="student_topic_selection")
@Data
public class StudentTopicSelection implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class) // 避免 id 过大前端出错
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 题目 id
     */
    private Long topicId;

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
     * 选题状态：0--预选，1--抢到
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}