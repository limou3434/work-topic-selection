package com.Lzh.answer.service.impl;

import com.Lzh.answer.common.ErrorCode;
import com.Lzh.answer.constant.CommonConstant;
import com.Lzh.answer.exception.BusinessException;
import com.Lzh.answer.model.dto.project.ProjectQueryRequest;
import com.Lzh.answer.model.dto.user.UserQueryRequest;
import com.Lzh.answer.model.entity.User;
import com.Lzh.answer.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.Lzh.answer.model.entity.Project;
import com.Lzh.answer.service.ProjectService;
import com.Lzh.answer.mapper.ProjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【project(系部)】的数据库操作Service实现
* @createDate 2024-06-12 10:34:21
*/
@Service
@Transactional
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
    implements ProjectService{

    /**
     * 获取查询条件
     *
     * @param projectQueryRequest
     * @param request
     * @return
     */
    @Override
    public QueryWrapper<Project> getQueryWrapper(ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        if (projectQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if(request==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "没登录");
        }
        String sortField = projectQueryRequest.getSortField();
        String sortOrder = projectQueryRequest.getSortOrder();
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(projectQueryRequest.getProjectName()), "projectName", projectQueryRequest.getProjectName());
        queryWrapper.like(StringUtils.isNotBlank(projectQueryRequest.getDeptName()), "deptName", projectQueryRequest.getDeptName());
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




