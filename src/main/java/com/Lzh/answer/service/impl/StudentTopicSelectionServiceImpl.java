package com.Lzh.answer.service.impl;

import com.Lzh.answer.mapper.StudentTopicSelectionMapper;
import com.Lzh.answer.model.entity.StudentTopicSelection;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.Lzh.answer.service.StudentTopicSelectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Administrator
* @description 针对表【student_topic_selection(学生选题)】的数据库操作Service实现
* @createDate 2024-05-22 15:21:19
*/
@Service
@Transactional
public class StudentTopicSelectionServiceImpl extends ServiceImpl<StudentTopicSelectionMapper, StudentTopicSelection>
    implements StudentTopicSelectionService{

}




