package com.Lzh.answer.model.dto.topic;

import com.Lzh.answer.common.PageRequest;
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
    List<UpdateTopicListRequest> updateTopicListRequests;
    private static final long serialVersionUID = 1L;
}
