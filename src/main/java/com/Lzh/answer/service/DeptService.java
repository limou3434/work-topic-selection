package com.Lzh.answer.service;

import com.Lzh.answer.model.dto.dept.DeptQueryRequest;
import com.Lzh.answer.model.enums.Dept;
import com.Lzh.answer.model.vo.DeptVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【dept(系部)】的数据库操作Service
* @createDate 2024-05-22 10:27:28
*/
public interface DeptService extends IService<Dept> {

    QueryWrapper<Dept> getDeptQueryWrapper(DeptQueryRequest deptQueryRequest, HttpServletRequest request);
}
