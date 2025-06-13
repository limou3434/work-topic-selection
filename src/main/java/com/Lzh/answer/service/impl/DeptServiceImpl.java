package com.Lzh.answer.service.impl;

import com.Lzh.answer.common.ErrorCode;
import com.Lzh.answer.constant.CommonConstant;
import com.Lzh.answer.exception.BusinessException;
import com.Lzh.answer.mapper.DeptMapper;
import com.Lzh.answer.model.dto.dept.DeptQueryRequest;
import com.Lzh.answer.model.enums.Dept;
import com.Lzh.answer.model.vo.DeptVO;
import com.Lzh.answer.service.DeptService;
import com.Lzh.answer.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【dept(系部)】的数据库操作Service实现
* @createDate 2024-05-22 10:27:28
*/
@Service
@Transactional
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept>
    implements DeptService{


    @Override
    public QueryWrapper<Dept> getDeptQueryWrapper(DeptQueryRequest deptQueryRequest, HttpServletRequest request) {
        if(request==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "没登录");
        }
        String sortField = deptQueryRequest.getSortField();
        String sortOrder = deptQueryRequest.getSortOrder();
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(deptQueryRequest.getDeptName()), "deptName", deptQueryRequest.getDeptName());
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




