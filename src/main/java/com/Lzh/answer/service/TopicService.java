package com.Lzh.answer.service;

import com.Lzh.answer.model.dto.topic.TopicQueryByAdminRequest;
import com.Lzh.answer.model.dto.topic.TopicQueryRequest;
import com.Lzh.answer.model.entity.Topic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【topic】的数据库操作Service
* @createDate 2024-05-22 14:41:18
*/
public interface TopicService extends IService<Topic> {

    QueryWrapper<Topic> getTopicQueryWrapper(TopicQueryRequest topicQueryRequest, HttpServletRequest request);

    QueryWrapper<Topic> getTopicQueryByAdminWrapper(TopicQueryByAdminRequest topicQueryByAdminRequest, HttpServletRequest request);
}
