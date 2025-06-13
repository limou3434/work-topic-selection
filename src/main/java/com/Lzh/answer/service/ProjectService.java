package com.Lzh.answer.service;

import com.Lzh.answer.model.dto.project.ProjectQueryRequest;
import com.Lzh.answer.model.dto.user.UserQueryRequest;
import com.Lzh.answer.model.entity.Project;
import com.Lzh.answer.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【project(系部)】的数据库操作Service
* @createDate 2024-06-12 10:34:21
*/
public interface ProjectService extends IService<Project> {
    /**
     * 获取查询条件
     *
     * @param projectQueryRequest
     * @param request
     * @return
     */
    QueryWrapper<Project> getQueryWrapper(ProjectQueryRequest projectQueryRequest, HttpServletRequest request);
}
