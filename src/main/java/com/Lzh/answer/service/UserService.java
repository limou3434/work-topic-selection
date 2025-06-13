package com.Lzh.answer.service;

import com.Lzh.answer.model.dto.dept.DeptQueryRequest;
import com.Lzh.answer.model.dto.user.DeptTeacherQueryRequest;
import com.Lzh.answer.model.entity.User;
import com.Lzh.answer.model.vo.DeptTeacherVO;
import com.Lzh.answer.model.vo.TeacherVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.Lzh.answer.model.dto.user.UserQueryRequest;
import com.Lzh.answer.model.vo.LoginUserVO;
import com.Lzh.answer.model.vo.UserVO;
import org.apache.poi.ss.formula.ptg.DeletedArea3DPtg;

import java.util.List;
import javax.servlet.http.HttpServletRequest;


/**
 * 用户服务
 *
 *  
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param updatePassword 校验密码
     * @return 新用户 id
     */
    long userUpdatePassword(String userAccount, String userPassword, String updatePassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest, HttpServletRequest request);
}
