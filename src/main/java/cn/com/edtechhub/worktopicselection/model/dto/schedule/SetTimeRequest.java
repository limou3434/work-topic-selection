package cn.com.edtechhub.worktopicselection.model.dto.schedule;

import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Lzh
 * @TableName topic
 */
@Data
public class SetTimeRequest implements Serializable {
    List<Topic> topicList;
    /**
     * 开启时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date endTime;

    private static final long serialVersionUID = 1L;
}