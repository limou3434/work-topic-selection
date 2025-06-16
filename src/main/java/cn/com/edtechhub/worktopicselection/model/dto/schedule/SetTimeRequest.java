package cn.com.edtechhub.worktopicselection.model.dto.schedule;

import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 设置选题开放时间
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class SetTimeRequest implements Serializable {

    /**
     * 选题列表
     */
    List<Topic> topicList;

    /**
     * 开启时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /// 序列化字段 ///
    private static final long serialVersionUID = 1L;

}