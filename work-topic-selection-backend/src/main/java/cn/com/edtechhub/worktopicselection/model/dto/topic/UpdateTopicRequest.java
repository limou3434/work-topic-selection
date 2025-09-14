package cn.com.edtechhub.worktopicselection.model.dto.topic;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 修改题目请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UpdateTopicRequest extends PageRequest implements Serializable {

    /**
     * 需要修改的题目请求列表
     */
    List<UpdateTopicListRequest> updateTopicListRequests;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
