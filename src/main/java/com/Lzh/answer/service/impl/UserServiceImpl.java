package com.Lzh.answer.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.Lzh.answer.constant.CommonConstant;
import com.Lzh.answer.constant.UserConstant;
import com.Lzh.answer.exception.BusinessException;
import com.Lzh.answer.mapper.UserMapper;
import com.Lzh.answer.model.dto.user.DeptTeacherQueryRequest;
import com.Lzh.answer.model.entity.Topic;
import com.Lzh.answer.model.entity.User;
import com.Lzh.answer.model.vo.DeptTeacherVO;
import com.Lzh.answer.model.vo.TeacherVO;
import com.Lzh.answer.service.TopicService;
import com.Lzh.answer.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.Lzh.answer.common.ErrorCode;
import com.Lzh.answer.model.dto.user.UserQueryRequest;
import com.Lzh.answer.model.vo.LoginUserVO;
import com.Lzh.answer.model.vo.UserVO;
import com.Lzh.answer.utils.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 *  
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "edtechhub";


    @Override
    public long userUpdatePassword(String userAccount, String userPassword, String updatePassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, updatePassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || updatePassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和更新密码相同
        if (userPassword.equals(updatePassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            final User user = this.baseMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }

            // 2. 校验原密码
            String oldEncryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            if (!oldEncryptPassword.equals(user.getUserPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
            }

            // 3. 加密新密码
            String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + updatePassword).getBytes());

            // 4. 更新用户密码
            user.setUserPassword(newEncryptPassword);
            user.setStatus("老用户");
            boolean saveResult = this.updateById(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败，数据库错误");
            }
            return user.getId();
        }
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if(StringUtils.isBlank(user.getStatus())){
            throw new BusinessException(50003, "用户为第一次登录,请修改密码");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        log.debug("【limou3434】检查 user {}, 登陆状态 {}", user, request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE));
        return this.getLoginUserVO(user);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if(request==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "没登录");
        }
        String userAccount = userQueryRequest.getUserAccount();
        final Integer userRole = userQueryRequest.getUserRole();
        String userName = userQueryRequest.getUserName();
        String dept = userQueryRequest.getDept();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.eq("userRole",userRole);
        queryWrapper.like(StringUtils.isNotBlank(dept), "dept", dept);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}
