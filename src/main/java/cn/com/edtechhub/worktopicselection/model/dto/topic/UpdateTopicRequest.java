package cn.com.edtechhub.worktopicselection.model.dto.topic;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Lzh
 * @TableName topic
 */
@Data
public class UpdateTopicRequest extends PageRequest implements Serializable {

    /**
     * 需要修改的题目请求列表
     */
    List<UpdateTopicListRequest> updateTopicListRequests;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}
