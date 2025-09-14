package cn.com.edtechhub.worktopicselection.model.dto.topic;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加数量请求体
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class AddCountRequest implements Serializable {

    /**
     * 题目id
     */
    private long id;

    /**
     * 加或者减
     */
    private int count;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}