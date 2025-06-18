package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.TopicConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.model.enums.StudentTopicSelectionStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.TopicStatusEnum;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.mapper.UserMapper;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeleteDeptRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.DeleteProjectRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.schedule.SetTimeRequest;
import cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection.SelectStudentRequest;
import cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection.SelectTopicByIdRequest;
import cn.com.edtechhub.worktopicselection.model.dto.topic.*;
import cn.com.edtechhub.worktopicselection.model.dto.user.*;
import cn.com.edtechhub.worktopicselection.model.entity.Project;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.entity.Dept;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.model.vo.*;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.*;
import cn.com.edtechhub.worktopicselection.utils.DeviceUtils;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户网络接口
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Transactional
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 引入用户服务依赖
     */
    @Resource
    private UserService userService;

    /**
     * 引入系部服务依赖
     */
    @Resource
    private DeptService deptService;

    /**
     * 引入专业服务依赖
     */
    @Resource
    private ProjectService projectService;

    /**
     * 引入选题服务依赖
     */
    @Resource
    private TopicService topicService;

    /**
     * 引入学生选题关联服务依赖
     */
    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    /// 用户相关接口 ///

    // 用户登入
    /*@PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(userLoginRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 用户登陆
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的必要信息");
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }*/
    @SaIgnore
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(userLoginRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = userLoginRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的账号");

        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的密码");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在, 请发邮件 898738804@qq.com 向管理员确认您的账号是否未被学院导入");

        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes()); // 提前得到加密密码
        ThrowUtils.throwIf(!Objects.equals(user.getUserPassword(), encryptPassword), CodeBindMessageEnums.PARAMS_ERROR, "用户密码不正确");

        // 用户登陆
        String device = DeviceUtils.getRequestDevice(request); // 登陆设备
        StpUtil.login(user.getId(), device); // 开始登录
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, user); // 把用户的信息存储到 Sa-Token 的会话中, 这样后续的用权限判断就不需要一直查询 SQL 才能得到, 缺点是更新权限的时候需要把用户踢下线否则会话无法更新

        // 数据脱敏
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    // 用户注销
    /*@PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 注销用户
        boolean result = userService.userLogout(request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
    }*/
    @SaCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 注销用户
        StpUtil.logout(); // 默认所有设备都被登出
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    // 创建用户
    /*@PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(userAddRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, CodeBindMessageEnums.PARAMS_ERROR, "没有登陆无法使用该接口");

//        Integer userRole = loginUser.getUserRole();
//        ThrowUtils.throwIf(!Objects.equals(userRole, UserRoleEnumssd.ADMIN.getValue()), CodeBindMessageEnums.NO_AUTH_ERROR, "只有管理员才能动用该接口");

        String userAccount = userAddRequest.getUserAccount();
        User oldUser = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(oldUser != null, CodeBindMessageEnums.PARAMS_ERROR, "该用户已存在, 无法重复添加");

        // 创建新的用户实例
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setDept(userAddRequest.getDeptName());
        user.setProject(userAddRequest.getProject());
        user.setUserRole(userAddRequest.getUserRole());
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
        user.setUserPassword(encryptPassword); // 设置默认密码

        // 添加新的用户
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "添加新的用户失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getId());
    }*/
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(userAddRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userName = userAddRequest.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户名字");

        String userAccount = userAddRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户学号/工号");

        User oldUser = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(oldUser != null, CodeBindMessageEnums.PARAMS_ERROR, "该学号/工号对于的用户已经存在, 请不要重复添加");

        Integer userRole = userAddRequest.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "缺少用户角色");

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "不存在该用户角色");

        // 如果是添加学生则需要检查是否设置了系部和专业
        if (userRoleEnum == UserRoleEnum.STUDENT) {
            String userDeptName = userAddRequest.getDeptName();
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");

            String userProject = userAddRequest.getProject();
            ThrowUtils.throwIf(StringUtils.isBlank(userProject), CodeBindMessageEnums.PARAMS_ERROR, "缺少专业名称");
        }

        // 如果是添加主任或教师则需要检查是否设置了系部和专业
        if (userRoleEnum == UserRoleEnum.DEPT || userRoleEnum == UserRoleEnum.TEACHER) {
            String userDeptName = userAddRequest.getDeptName();
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
        }

        // 创建新的用户实例
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setDept(userAddRequest.getDeptName());
        user.setProject(userAddRequest.getProject());
        user.setUserRole(userAddRequest.getUserRole());
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
        user.setUserPassword(encryptPassword); // 设置默认密码

        // 添加新的用户
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "添加新的用户失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getId());
    }

    // 删除用户
    /*@PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(deleteRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = deleteRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        // 删除用户
        boolean result = userService.remove(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.NOT_FOUND_ERROR, "删除用户失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
    }*/
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(deleteRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = deleteRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在无需删除");

        // 删除用户
        boolean result = userService.removeById(user.getId());
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "删除用户失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
    }

    // 更新用户
    /*@PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(userUpdateRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        ThrowUtils.throwIf(userUpdateRequest.getId() == null, CodeBindMessageEnums.PARAMS_ERROR, "没有用户标识无法更新");

        // 创建更新后的新用户实例
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }*/
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(userUpdateRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(id < 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识不合法, 必须为正整数");

        // 创建更新后的新用户实例
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    // 重置用户密码
    /*
    @PostMapping("reset/password")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (resetPasswordRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求数据为空");
        }
        String userName = resetPasswordRequest.getUserName();
        String userAccount = resetPasswordRequest.getUserAccount();
        final User student = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userName", userName));
        if (student == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "该用户不存在");
        }
        final String SALT = "yupi";
        String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + "12345678").getBytes());
        student.setUserPassword(newEncryptPassword);
        student.setStatus(" ");
        final boolean b = userService.updateById(student);
        if (!b) {
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "修改失败");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, student.getUserAccount());
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/reset/password")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(resetPasswordRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userName = resetPasswordRequest.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "用户名不能为空");

        String userAccount = resetPasswordRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        User user = userService.getOne(
                new QueryWrapper<User>()
                        .eq("userAccount", userAccount)
                        .eq("userName", userName)
        );
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该用户不存在, 无需重置密码");

        // 获取新的初始化密码
        String newEncryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
        user.setUserPassword(newEncryptPassword);
        user.setStatus(null); // 状态置空, 以方便后续强制要求用户重新登陆
        final boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改用户密码失败");

        // 强制要求用户重新登陆并且清理缓存
        StpUtil.logout(user.getId()); // 默认该用户的所有设备都被登出
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getUserAccount());
    }

    // 用户自己手动修改密码
    /*
    @PostMapping("/register")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword userUpdatePassword) {
        // TODO: 实际上本项目不存在注册新用户, 只能管理员由管理员手动导入系统, 这是因为学院系统的特殊性, 不过这个接口是用来修改用户的初始化密码用的...

        // 检查参数
        ThrowUtils.throwIf(userUpdatePassword == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 更新用户
        String userAccount = userUpdatePassword.getUserAccount();
        String userPassword = userUpdatePassword.getUserPassword();
        final String updatePassword = userUpdatePassword.getUpdatePassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, updatePassword)) {
            return null;
        } // TODO: 666, 这个逻辑这么写还改不了, 因为鬼知道他是不是后面复用了这个接口...我个人不推荐在接口类内部复用接口的, 除非作接口的强化, 这么写很搞人...
        long result = userService.userUpdatePassword(userAccount, userPassword, updatePassword);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
    }
    */
    @SaIgnore
    @PostMapping("/updata/password")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword userUpdatePassword) {
        // 检查参数
        ThrowUtils.throwIf(userUpdatePassword == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = userUpdatePassword.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户学号/工号不能为空");

        String userPassword = userUpdatePassword.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "用户密码不能为空");

        String updatePassword = userUpdatePassword.getUpdatePassword();
        ThrowUtils.throwIf(StringUtils.isBlank(updatePassword), CodeBindMessageEnums.PARAMS_ERROR, "新密码不能为空");

        // 必须通过学号/工号密码验证后才能修改密码
        User user = userService.getOne(
                new QueryWrapper<User>()
                        .eq("userAccount", userAccount)
                        .eq("userPassword", DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes()))
        );
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "旧密码不正确, 无法修改密码, 如果忘记旧密码请发送邮箱 898738804@qq.com 向联系管理员重置密码");

        // 更新用户
        user.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.SALT + updatePassword).getBytes()));
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改密码失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getId());
    }

    // 根据 id 获取用户数据
    /*@GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        // 检查参数
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "");

        // 返回用户信息
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user);
    }*/
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        // 检查参数
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 返回用户信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "不存在该用户");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user);
    }

    // 获取当前登录用户数据
    /*@GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }*/
    @SaCheckLogin
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        Long LoginUserId = userService.userGetCurrentLonginUserId();
        User user = userService.userGetSessionById(LoginUserId);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }

    // 获取用户分页数据
    /*
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/get/user/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 检查参数
        long current = userQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取用户数据
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }

    // 根据获取老师姓名
    /*
    @PostMapping("get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest teacherQueryRequest, HttpServletRequest request) {
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", teacherQueryRequest.getUserRole()));
        List<TeacherVO> teacherVOList = new ArrayList<>();
        for (User user : userList) {
            TeacherVO teacherVO = new TeacherVO();
            final String userName = user.getUserName();
            teacherVO.setLabel(userName);
            teacherVO.setValue(userName);
            teacherVOList.add(teacherVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherVOList);
    }
    */
    @SaCheckLogin
    @PostMapping("get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest teacherQueryRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(teacherQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Integer userRole = teacherQueryRequest.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "用户角色不能为空");

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "该用户角色不存在");

        // 获取所有的教师数据
        List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", userRoleEnum.getCode()));
        List<TeacherVO> teacherVOList = new ArrayList<>();
        for (User user : userList) {
            TeacherVO teacherVO = new TeacherVO();
            final String userName = user.getUserName();
            teacherVO.setLabel(userName);
            teacherVO.setValue(userName);
            teacherVOList.add(teacherVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherVOList);
    }

    // 根据 id 获取用户包装数据
    /*@GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        // 检查参数
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 把脱敏后的数据返回
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getUserVO(user));
    }*/
//    @SaCheckLogin
//    @GetMapping("/get/vo")
//    public BaseResponse<UserVO> getUserVOById(long id) {
//        // 检查参数
//        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");
//
//        // 获取用户数据
//        BaseResponse<User> response = getUserById(id);
//        User user = response.getData();
//
//        // 获取脱敏后的数据
//        UserVO userVO = new UserVO();
//        userVO = userService.getUserVO(user);
//        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVO);
//    }

    /// 系部专业相关接口 ///

    // 获取系部分页数据
    /*
    @PostMapping("get/dept")
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest deptQueryRequest, HttpServletRequest request) {
        long current = deptQueryRequest.getCurrent();
        long size = deptQueryRequest.getPageSize();
        Page<Dept> deptPage = deptService.page(new Page<>(current, size),
                deptService.getDeptQueryWrapper(deptQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptPage);
    }
    */
    @SaCheckLogin
    @PostMapping("/get/dept/page")
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest deptQueryRequest) {
        // 检查参数
        long current = deptQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = deptQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取系部数据
        Page<Dept> deptPage = deptService.page(new Page<>(current, size), deptService.getQueryWrapper(deptQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptPage);
    }

    // 获取系部列表数据
    /*
    @PostMapping("get/dept/list")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest deptQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(deptQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = deptQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页号不能小于 1");

        long size = deptQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小不能小于 1");

        Page<Dept> deptPage = deptService.page(new Page<>(current, size), deptService.getQueryWrapper(deptQueryRequest));
        List<DeptVO> deptVOList = new ArrayList<>();
        for (Dept dept : deptPage.getRecords()) {
            final String deptName = dept.getDeptName();
            final DeptVO deptVO = new DeptVO();
            deptVO.setLabel(deptName);
            deptVO.setValue(deptName);
            deptVOList.add(deptVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptVOList);
    }
    */
    @SaCheckLogin
    @PostMapping("/get/dept/list")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest deptQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(deptQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 查询所有 dept 列表
        List<Dept> deptList = deptService.list();

        // 脱敏数据
        List<DeptVO> deptVOList = new ArrayList<>();
        for (Dept dept : deptList) {
            DeptVO deptVO = new DeptVO();
            deptVO.setLabel(dept.getDeptName());
            deptVO.setValue(dept.getDeptName());
            deptVOList.add(deptVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptVOList);
    }

    // 获取专业分页数据
    /*
    @PostMapping("get/project")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        long current = projectQueryRequest.getCurrent();
        long size = projectQueryRequest.getPageSize();
        Page<Project> projectPage = projectService.page(new Page<>(current, size),
                projectService.getQueryWrapper(projectQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }
    */
    @SaCheckLogin
    @PostMapping("/get/project/page")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        // 检查参数
        long current = projectQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = projectQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(projectQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }

    // 添加系部
    /*
    @PostMapping("/add/dept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest deptAddRequest, HttpServletRequest request) {
        if (deptAddRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
//        if (userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        final String deptName = deptAddRequest.getDeptName();
        final Dept dept = deptService.getOne(new QueryWrapper<Dept>().eq("deptName", deptName));
        if (dept != null) {
            return TheResult.error(CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该系部已存在");
        }
        Dept newDept = new Dept();
        newDept.setDeptName(deptName);
        boolean result = deptService.save(newDept);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, newDept.getId());
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/dept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest deptAddRequest) {
        // 检查参数
        ThrowUtils.throwIf(deptAddRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String deptName = deptAddRequest.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        Dept dept = deptService.getOne(new QueryWrapper<Dept>().eq("deptName", deptName));
        ThrowUtils.throwIf(dept != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该系部已存在, 请不要重复添加");

        // 添加新的系部
        Dept newDept = new Dept();
        newDept.setDeptName(deptName);
        boolean result = deptService.save(newDept);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的系部");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, newDept.getId());
    }

    // 添加专业
    /*
    @PostMapping("/add/project")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest projectAddRequest, HttpServletRequest request) {
        if (projectAddRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
//        if (userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        final String projectName = projectAddRequest.getProjectName();
        final String deptName = projectAddRequest.getDeptName();
        final Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", projectName));
        if (project != null) {
            return TheResult.error(CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该专业已存在");
        }

        Project newProject = new Project();
        newProject.setProjectName(projectName);
        newProject.setDeptName(deptName);

        boolean result = projectService.save(newProject);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, newProject.getId());
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/project")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest projectAddRequest) {
        // 检查参数
        ThrowUtils.throwIf(projectAddRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String projectName = projectAddRequest.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        String deptName = projectAddRequest.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", projectName));
        ThrowUtils.throwIf(project != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该专业已存在, 请不要重复添加");

        // 添加新的专业
        Project newProject = new Project();
        newProject.setProjectName(projectName);
        newProject.setDeptName(deptName);
        boolean result = projectService.save(newProject);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的专业");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, newProject.getId());
    }

    /// 选题相关接口 ///

    // 获取选题分页数据
    /*
    @SaCheckLogin
    @PostMapping("/get/topic/page")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest topicQueryRequest) {
        // 检查参数
        long current = topicQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = topicQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取选题数据
        Page<Topic> topicPage = topicService.page(new Page<>(current, size), topicService.getQueryWrapper(topicQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }
    */
    @SaCheckLogin
    @PostMapping("/get/topic/page")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest topicQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(topicQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = topicQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = topicQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取选题数据
        Page<Topic> topicPage = topicService.page(new Page<>(current, size), topicService.getQueryWrapper(topicQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }

    // 添加选题
    /*
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest addTopicRequest, HttpServletRequest request) {
        if (addTopicRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
//        if (userRole != 1 && userRole != 2 && userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        final Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", addTopicRequest.getTopic()));
        if (oldTopic != null) {
            return TheResult.error(CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该课题已存在");
        }
        final Topic topic = new Topic();
        BeanUtils.copyProperties(addTopicRequest, topic);
//        if (userRole == 1) {
//            topic.setTeacherName(loginUser.getUserName());
//        }
        topic.setSurplusQuantity(addTopicRequest.getAmount());
        boolean result = topicService.save(topic);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest addTopicRequest) {
        // 检查参数
        ThrowUtils.throwIf(addTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", addTopicRequest.getTopic()));
        ThrowUtils.throwIf(oldTopic != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该选题已存在, 请不要重复添加");

        // 添加新的选题
        User loginUser = userService.userGetCurrentLoginUser();
        Topic topic = new Topic();
        BeanUtils.copyProperties(addTopicRequest, topic);
        topic.setTeacherName(loginUser.getUserName());
        topic.setSurplusQuantity(addTopicRequest.getAmount());
        boolean result = topicService.save(topic);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的选题");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
    }

    // 获取当前的选题情况(只能获取和当前登陆用户系部相同的选题)
    /*
    @PostMapping("get/select/topic/situation")
    public BaseResponse<SituationVO> getSelectTopicSituation(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }

        // 获取所有学生的总人数和已选题人数
        final int totalStudents = (int) userService.count(new QueryWrapper<User>().eq("userRole", 0).eq(StringUtils.isNotBlank(loginUser.getDept()), "dept", loginUser.getDept()));
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", 0).eq(StringUtils.isNotBlank(loginUser.getDept()), "dept", loginUser.getDept()));

        int selectedStudents = 0;
        for (User user : userList) {
            final String userAccount = user.getUserAccount();
            selectedStudents += (int) studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("status", 1).eq("userAccount", userAccount));
        }

        final int unselectedStudents = totalStudents - selectedStudents;

        // 封装返回数据
        SituationVO situationVO = new SituationVO();
        situationVO.setSelectAmount(selectedStudents);
        situationVO.setAmount(totalStudents);
        situationVO.setUnselectAmount(unselectedStudents);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, situationVO);
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/situation")
    public BaseResponse<SituationVO> getSelectTopicSituation() {
        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 获取总人数
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("userRole", UserRoleEnum.STUDENT.getCode()) // 只获取学生记录
                .eq(!userService.userIsAdmin(loginUser), "dept", loginUser.getDept()) // 获取当前登陆用户系部相同的学生, 但是管理员可以获取所有学生
        ;
        int totalStudents = (int) userService.count(queryWrapper);
        List<User> userList = userService.list(queryWrapper);

        // 获取已选题人数
        int selectedStudents = 0;
        for (User user : userList) {
            final String userAccount = user.getUserAccount();
            selectedStudents += (int) studentTopicSelectionService.count(
                    new QueryWrapper<StudentTopicSelection>()
                            .eq("userAccount", userAccount) // 获取当前用户的记录
                            .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()) // 查询状态为已选题的
            ); // TODO: 这个查询过程可以被优化, 但是暂时先这样
        }

        // 获取未选题人数
        int unselectedStudents = totalStudents - selectedStudents;

        // 封装返回数据
        SituationVO situationVO = new SituationVO();
        situationVO.setSelectAmount(selectedStudents);
        situationVO.setAmount(totalStudents);
        situationVO.setUnselectAmount(unselectedStudents);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, situationVO);
    }

    // 根据题目 id 添加开放的开始时间和结束时间
    /*
    @PostMapping("set/time/by/id")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest setTimeRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
//        if (userRole != 2 && userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        if (setTimeRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        Date startTime = setTimeRequest.getStartTime();
        Date endTime = setTimeRequest.getEndTime();
        for (Topic topic : setTimeRequest.getTopicList()) {
            final Long id = topic.getId();
            final Topic topic1 = topicService.getById(id);
            topic1.setStatus("1");
            topic1.setStartTime(startTime);
            topic1.setEndTime(endTime);
            final boolean b = topicService.updateById(topic1);
            if (!b) {
                throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
            }
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "");
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/time/by/id")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest setTimeRequest) {
        // 检查参数
        ThrowUtils.throwIf(setTimeRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        List<Topic> topicList = setTimeRequest.getTopicList();
        ThrowUtils.throwIf(topicList == null || topicList.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "请先选择要开放的题目");

        Date startTime = setTimeRequest.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择开始时间");

        Date endTime = setTimeRequest.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择结束时间");

        // 遍历选题列表开始设置开始时间和结束时间
        for (Topic topic : setTimeRequest.getTopicList()) {
            topic.setStatus(TopicStatusEnum.PUBLISHED.getCode());
            topic.setStartTime(startTime);
            topic.setEndTime(endTime);
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法开放该选题, 请联系管理员 898738804@qq.com");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "开始选题!");
    }

    // 审核题目
    /*
    @PostMapping("/check/topic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest checkTopicRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (checkTopicRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "请求参数为空");
        }

        Long id = checkTopicRequest.getId();
        String status = checkTopicRequest.getStatus();
        final String reason = checkTopicRequest.getReason();
        final Topic topic = topicService.getById(id);
        if (topic == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的题目");
        }

        if (status.equals("-1")) {
            final String currentStatus = topic.getStatus();
            if (!currentStatus.equals("-2")) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "当前题目状态不允许此操作");
            }
        }
        if (reason != null) {
            topic.setReason(reason);
        }
        topic.setStatus(status);
        final boolean updated = topicService.updateById(topic);
        if (!updated) {
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "更新题目状态失败");
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/check/topic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest checkTopicRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(checkTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long id = checkTopicRequest.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 不能为空");
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 必须是正整数");

        Integer status = checkTopicRequest.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "选题状态不能为空");

        TopicStatusEnum statusEnum = TopicStatusEnum.getEnums(status);
        ThrowUtils.throwIf(statusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "未知的选题状态");

        String reason = checkTopicRequest.getReason();
        ThrowUtils.throwIf(reason != null && reason.length() > TopicConstant.MAX_REASON_SIZE, CodeBindMessageEnums.PARAMS_ERROR, "理由不能超过 1024 个字符");

        Topic topic = topicService.getById(id);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "对应的选题不存在, 无需进行审核");

        Integer currentStatus = topic.getStatus();
        TopicStatusEnum currentStatusEnum = TopicStatusEnum.getEnums(currentStatus);
        ThrowUtils.throwIf(currentStatusEnum == statusEnum, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "请不要重复设置相同的状态");

        // 更新选题审核内容
        topic.setStatus(status);
        if (reason != null) {
            topic.setReason(reason);
        }
        boolean result = topicService.updateById(topic);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新题目状态失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    // 获取系部教师数据
    /*
    @PostMapping("get/dept/teacher")
    public BaseResponse<Page<DeptTeacherVO>> getTeacher(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest, HttpServletRequest request) {
        if (deptTeacherQueryRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");
        }
        long current = deptTeacherQueryRequest.getCurrent();
        long size = deptTeacherQueryRequest.getPageSize();
        String sortField = deptTeacherQueryRequest.getSortField();
        String sortOrder = deptTeacherQueryRequest.getSortOrder();
        final User loginUser = userService.getLoginUser(request);
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
        if (studentTopicSelection != null) {
            return TheResult.error(CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经选择过课题");
        }
        final String dept = loginUser.getDept();

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("dept", dept);
//        userQueryWrapper.eq("userRole", 1);
        userQueryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        List<User> users = userMapper.selectList(userQueryWrapper);

        // 创建返回的 Page 对象
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            final String userName = user.getUserName();

            // 查询该用户的课题列表
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.eq("teacherName", userName);
            topicQueryWrapper.eq("deptName", loginUser.getDept());
            topicQueryWrapper.eq("status", "1");
            int count = (int) topicService.count(topicQueryWrapper);
            List<Topic> topicList = topicService.list(topicQueryWrapper);

            // 计算剩余数量和选择数量
            Integer surplusQuantity = 0;
            Integer selectAmount = 0;
            for (Topic topic : topicList) {
                surplusQuantity += topic.getSurplusQuantity();
                selectAmount += topic.getSelectAmount();
            }
            if (count != 0) {
                // 构建 DeptTeacherVO 对象
                DeptTeacherVO teacherVO = new DeptTeacherVO();
                teacherVO.setTeacherName(userName);
                teacherVO.setSurplusQuantity(surplusQuantity);
                teacherVO.setSelectAmount(selectAmount);
                teacherVO.setTopicAmount(count);
                teacherVOList.add(teacherVO);
            }
            // 添加到列表中
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(teacherVOList);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }@PostMapping("get/dept/teacher")
    */
    @SaCheckLogin
    @PostMapping("/get/dept/teacher")
    public BaseResponse<Page<DeptTeacherVO>> getTeacher(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(deptTeacherQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = deptTeacherQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "当前页码必须大于 0");

        long size = deptTeacherQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "每页大小必须大于 0");

        String sortField = deptTeacherQueryRequest.getSortField();

        String sortOrder = deptTeacherQueryRequest.getSortOrder();

        // 查询用户列表, 但是只查询自己这个系部的教师
        User loginUser = userService.userGetCurrentLoginUser();
        String dept = loginUser.getDept();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper
                .eq("dept", dept) // 同系的
                .eq("userRole", UserRoleEnum.TEACHER.getCode()) // 是教师的
                .orderBy(
                        SqlUtils.validSortField(sortField),
                        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                        sortField
                )
        ;
        List<User> users = userMapper.selectList(userQueryWrapper); // 得到所有的教师

        // 利用教师列表来创建返回的 Page 对象, 填充每位教师的选题情况
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            // 获得教师的名字
            String userName = user.getUserName();

            // 获得教师的对应选题
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper
                    .eq("teacherName", userName)
                    .eq("deptName", loginUser.getDept())
                    .eq("status", TopicStatusEnum.PUBLISHED.getCode()) // 学生只能查看已经开放的选题
            ;
            int count = (int) topicService.count(topicQueryWrapper);
            List<Topic> topicList = topicService.list(topicQueryWrapper);

            // 计算剩余数量和选择数量
            Integer selectAmount = 0;
            Integer surplusQuantity = 0;
            for (Topic topic : topicList) {
                selectAmount += topic.getSelectAmount();
                surplusQuantity += topic.getSurplusQuantity();
            }


            // 构建 DeptTeacherVO 对象
            if (count != 0) {
                DeptTeacherVO teacherVO = new DeptTeacherVO();
                teacherVO.setTeacherName(userName);
                teacherVO.setSurplusQuantity(surplusQuantity);
                teacherVO.setSelectAmount(selectAmount);
                teacherVO.setTopicAmount(count);
                teacherVOList.add(teacherVO);
            }
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(teacherVOList);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

    // TODO: ...

    // 根据题目 id 进行预先选题的操作(确认预先选题和取消预先选题)
    /*
    @PostMapping("pre/select/topic/by/id")
    public BaseResponse<Long> preSelectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        if (selectTopicByIdRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
        final Topic topic = topicService.getById(selectTopicByIdRequest.getId());
        if (selectTopicByIdRequest.getStatus() == 1) {
            if (selection != null) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
            }
            final Integer surplusQuantity = topic.getSurplusQuantity();
            if (surplusQuantity <= 0) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "选题余量不足");
            }
            final StudentTopicSelection studentTopicSelection = new StudentTopicSelection();
            studentTopicSelection.setUserAccount(loginUser.getUserAccount());
            studentTopicSelection.setTopicId(selectTopicByIdRequest.getId());
            final boolean save = studentTopicSelectionService.save(studentTopicSelection);
            if (!save) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
            }
        } else {
            if (selection == null) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
            }
            final boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
            if (!remove) {
                throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
            }
        }
        topic.setSelectAmount(topic.getSelectAmount() + selectTopicByIdRequest.getStatus());
        final boolean updateTopic = topicService.updateById(topic);
        if (!updateTopic) {
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
    }
    */
    @SaCheckLogin
    @PostMapping("/preselect/topic/by/id")
    public BaseResponse<Long> preSelectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest) {
        // 检查参数
        ThrowUtils.throwIf(selectTopicByIdRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = selectTopicByIdRequest.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

        Integer status = selectTopicByIdRequest.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "操作状态不能为空");

        StudentTopicSelectionStatusEnum studentTopicSelectionStatusEnum = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(studentTopicSelectionStatusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "不存在这种状态");

        // 设置题目剩余数量操作数字
        int opt = 0;

        // 获取当前登录用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 处理预先选题的操作
        if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.EN_PRESELECT) { // 确认预选题目
            // 限制预选数量
            int selectedCount = Math.toIntExact(studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount())));
            ThrowUtils.throwIf(selectedCount >= 5, CodeBindMessageEnums.OPERATION_ERROR, "最多只能预选 10 个题目");

            // 有选题余量才可以预选
            Integer surplusQuantity = topic.getSurplusQuantity();
            ThrowUtils.throwIf(surplusQuantity <= 0, CodeBindMessageEnums.OPERATION_ERROR, "选题余量不足, 无法选择该题目");

            // 创建关联记录
            StudentTopicSelection studentTopicSelection = new StudentTopicSelection();
            studentTopicSelection.setUserAccount(loginUser.getUserAccount());
            studentTopicSelection.setTopicId(topic.getId());
            boolean save = studentTopicSelectionService.save(studentTopicSelection);
            ThrowUtils.throwIf(!save, CodeBindMessageEnums.OPERATION_ERROR, "无法保存预选, 请联系管理员 898738804@qq.com");

            // 修改操作标志位
            opt = +1;
        } else if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.UN_PRESELECT) { // 取消预选题目
            // 修改关联记录
            boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
            if (!remove) {
                throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
            }

            // 修改操作标志位
            opt = -1;
        } else {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "该接口不允许传递这种状态, 请联系管理员 898738804@qq.com");
        }

        topic.setSelectAmount(topic.getSelectAmount() + opt);
        boolean updateTopic = topicService.updateById(topic);
        ThrowUtils.throwIf(!updateTopic, CodeBindMessageEnums.OPERATION_ERROR, "无法保存余量, 请联系管理员 898738804@qq.com");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
    }

    // 根据题目 id 进行提交选题的操作(确认提交选题和取消提交选题)
    /*
    @SaCheckLogin
    @PostMapping("/select/topic/by/id")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest) {
        // 检查参数
        ThrowUtils.throwIf(selectTopicByIdRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = selectTopicByIdRequest.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

        Date startTime = topic.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有传递开始时间");

        Date endTime = topic.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有传递结束时间");

        Date now = new Date();
        ThrowUtils.throwIf(now.before(startTime) || now.after(endTime), CodeBindMessageEnums.NOT_TIME_ERROR, "请等待管理员开放选题");

        Integer surplusQuantity = topic.getSurplusQuantity();
        ThrowUtils.throwIf(surplusQuantity <= 0, CodeBindMessageEnums.NOT_SURPLUS_ERROR, "");

        // 尝试获取学生选题关联表中的记录
        User loginUser = userService.userGetCurrentLoginUser();
        StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));

        // TODO: ...
        if (selectTopicByIdRequest.getStatus() == -1) {
            final boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
            if (!remove) {
                throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
            }
        } else {
            selection.setStatus(selectTopicByIdRequest.getStatus());
            final boolean update = studentTopicSelectionService.updateById(selection);
            if (!update) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
            }
        }
        topic.setSurplusQuantity(topic.getSurplusQuantity() - selectTopicByIdRequest.getStatus());
        topic.setSelectAmount(topic.getSelectAmount() - selectTopicByIdRequest.getStatus());
        final boolean updateTopic = topicService.updateById(topic);
        if (!updateTopic) {
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, selection.getId());
    }
    */
    @SaCheckLogin
    @PostMapping("/select/topic/by/id")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest) {
        // 检查参数
        ThrowUtils.throwIf(selectTopicByIdRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = selectTopicByIdRequest.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

        Date startTime = topic.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有传递开始时间");

        Date endTime = topic.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有传递结束时间");

        Date now = new Date();
        ThrowUtils.throwIf(now.before(startTime) || now.after(endTime), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前不在选题开放范围内, 请等待管理员开放选题");

        Integer status = selectTopicByIdRequest.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "请添加选择操作状态");

        StudentTopicSelectionStatusEnum statusEnums = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(statusEnums == null, CodeBindMessageEnums.PARAMS_ERROR, "该操作状态非法");

        // 设置题目剩余数量操作数字
        int opt = 0;

        // 尝试获取当前登陆学生选题关联表中的记录
        User loginUser = userService.userGetCurrentLoginUser();
        int selectedCount = Math.toIntExact(studentTopicSelectionService.count(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("userAccount", loginUser.getUserAccount()))
        );
        ThrowUtils.throwIf(selectedCount > 1, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经提交了题目, 不能再选新的题目了, 如果需要取消则需要联系导师");

        // 尝试获取学生选题关联表中的记录
        StudentTopicSelection selection = studentTopicSelectionService.getOne(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("userAccount", loginUser.getUserAccount())
                        .eq("topicId", topicId)
        );

        // TODO: 替换逻辑, 紧急!!!
        if (statusEnums == StudentTopicSelectionStatusEnum.EN_SELECT) {
            ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "余量不足无法选择该题目, 请尝试选择其他题目");
            ThrowUtils.throwIf(selection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "您还没有预选无法直接选中");

            // 更新题目状态
            selection.setStatus(selectTopicByIdRequest.getStatus());
            boolean update = studentTopicSelectionService.updateById(selection);
            ThrowUtils.throwIf(!update, CodeBindMessageEnums.OPERATION_ERROR, "无法提交选题, 请联系管理员 898738804@qq.com");

            // 修改操作标志位
            opt = -1;
        } else if (statusEnums == StudentTopicSelectionStatusEnum.UN_SELECT) {
            boolean remove = studentTopicSelectionService.remove(
                    new QueryWrapper<StudentTopicSelection>()
                            .eq("userAccount", loginUser.getUserAccount())
                            .eq("topicId", selectTopicByIdRequest.getId())
            );
            ThrowUtils.throwIf(!remove, CodeBindMessageEnums.NOT_FOUND_ERROR, "您还没有选择题目, 无法取消选择");

            // 修改操作标志位
            opt = +1;
        } else {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "该接口不允许传递这种状态, 请联系管理员 898738804@qq.com");
        }

        // 更新选题
        topic.setSurplusQuantity(topic.getSurplusQuantity() + opt);
        boolean updateTopic = topicService.updateById(topic);
        ThrowUtils.throwIf(!updateTopic, CodeBindMessageEnums.OPERATION_ERROR, "无法更新选题, 请联系管理员");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, selection.getId());
    }

    // 获取当前登陆账号学生的预先选题
    /*
    @PostMapping("get/pre/topic")
    public BaseResponse<List<Topic>> getPreTopic(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", 0));
        if (studentTopicSelection == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        final Long topicId = studentTopicSelection.getTopicId();
        final Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        topicList.add(topic);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/get/preselect/topic")
    public BaseResponse<List<Topic>> getPreTopic() {
        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 查询对应的预先选题记录
        String userAccount = loginUser.getUserAccount();
        ThrowUtils.throwIf(userAccount == null, CodeBindMessageEnums.OPERATION_ERROR, "参数有问题");
        List<StudentTopicSelection> studentTopicSelectionList = studentTopicSelectionService.list(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("userAccount", userAccount)
                        .eq("status", StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode())
        );
        ThrowUtils.throwIf(studentTopicSelectionList == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前没有预选的题目");

        // 填充完整的返回体, 把关联对应的选题都拿到
        List<Long> topicIds = studentTopicSelectionList.stream()
                .map(StudentTopicSelection::getTopicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Topic> topicList = topicService.listByIds(topicIds);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    // 获取当前登陆账号学生的最终选题
    /*
    @PostMapping("get/select/topic")
    public BaseResponse<List<Topic>> getSelectTopic(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", 1));
        if (studentTopicSelection == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        final Long topicId = studentTopicSelection.getTopicId();
        final Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        if (topic == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        topicList.add(topic);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("get/select/topic")
    public BaseResponse<List<Topic>> getSelectTopic() {
        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        String userAccount = loginUser.getUserAccount();

        // 查找确认最终选题的记录
        StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("userAccount", userAccount)
                        .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
        );
        log.info("用户: {} 确认了最终选题", userAccount);

        ThrowUtils.throwIf(studentTopicSelection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前用户没有确认最终的选题");

        // 封装最终的返回值
        Long topicId = studentTopicSelection.getTopicId();
        Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.OPERATION_ERROR, "不存在对应的题目, 请联系管理员 898738804@qq.com");
        topicList.add(topic);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    // 更新选题信息
    /*
        @PostMapping("/update/topic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest updateTopicRequest) {
        long current = updateTopicRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "当前页码必须大于 0");

        long size = updateTopicRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "每页大小必须大于 0");

        List<UpdateTopicListRequest> updateTopicListRequests = updateTopicRequest.getUpdateTopicListRequests();

        // 获取当前用户
        User loginUser = userService.userGetCurrentLoginUser();
        final String teacherName = loginUser.getUserName();

        // 获取教师的分页topics
        Page<Topic> page = new Page<>(current, size);
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherName", teacherName);
        IPage<Topic> teacherTopicsPage = topicService.page(page, queryWrapper);
        List<Topic> teacherTopics = teacherTopicsPage.getRecords();

        // 获取updateTopicRequestList中的所有id
        Set<Long> updateTopicIds = updateTopicListRequests.stream()
                .map(UpdateTopicListRequest::getId)
                .collect(Collectors.toSet());

        // 过滤teacherTopics中不在updateTopicIds中的项
        List<Topic> topicsToRemove = teacherTopics.stream()
                .filter(topic -> !updateTopicIds.contains(topic.getId()))
                .collect(Collectors.toList());

        // 删除topicsToRemove中的所有项
        if (!topicsToRemove.isEmpty()) {
            List<Long> topicsToRemoveIds = topicsToRemove.stream()
                    .map(Topic::getId)
                    .collect(Collectors.toList());
            topicService.removeByIds(topicsToRemoveIds);
            // 删除与这些主题相关的学生选择记录
            for (Long topicId : topicsToRemoveIds) {
                studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topivId", topicId));
            }
        }

        // 更新updateTopicListRequests中的项
        for (UpdateTopicListRequest updateRequest : updateTopicListRequests) {
            Topic topic = new Topic();
            BeanUtils.copyProperties(updateRequest, topic);
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, "更新成功");
    }
    */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/update/topic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest updateTopicRequest) {
        // 检查参数
        ThrowUtils.throwIf(updateTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = updateTopicRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "当前页码必须大于 0");

        long size = updateTopicRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "每页大小必须大于 0");

        // 获取当前登陆的教师
        User loginUser = userService.userGetCurrentLoginUser();
        final String teacherName = loginUser.getUserName();

        // 获取当前登陆教师所提交的所有选题分页数据
        Page<Topic> page = new Page<>(current, size);
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherName", teacherName);
        IPage<Topic> teacherTopicsPage = topicService.page(page, queryWrapper);
        List<Topic> teacherTopics = teacherTopicsPage.getRecords();

        // 处理请求列表中的所有 id(也就是前端表格中的所有列的数据)
        List<UpdateTopicListRequest> updateTopicListRequests = updateTopicRequest.getUpdateTopicListRequests();
        Set<Long> updateTopicIds = updateTopicListRequests
                .stream()
                .map(UpdateTopicListRequest::getId)
                .collect(Collectors.toSet());

        // 过滤 teacherTopics 中不在 updateTopicIds 中的项
        List<Topic> topicsToRemove = teacherTopics
                .stream()
                .filter(topic -> !updateTopicIds.contains(topic.getId()))
                .collect(Collectors.toList());

        // 删除 topicsToRemove 中的所有项
        if (!topicsToRemove.isEmpty()) {
            List<Long> topicsToRemoveIds = topicsToRemove.stream()
                    .map(Topic::getId)
                    .collect(Collectors.toList());
            topicService.removeByIds(topicsToRemoveIds);
            // 删除与这些主题相关的学生选择记录
            for (Long topicId : topicsToRemoveIds) {
                studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topivId", topicId));
            }
        }

        // 更新 updateTopicListRequests 中的项
        for (UpdateTopicListRequest updateRequest : updateTopicListRequests) {
            Topic topic = new Topic();
            BeanUtils.copyProperties(updateRequest, topic);
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, "更新成功");
    }

    /// TODO: 下面都是旧代码...并且把所有的权限都去除了

    @Resource
    private UserMapper userMapper;

    /**
     * 分页获取用户封装列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        // 参数检查
        ThrowUtils.throwIf(userQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, CodeBindMessageEnums.PARAMS_ERROR, "不能一次性获取过多的分页数据");

        long current = userQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 0, CodeBindMessageEnums.PARAMS_ERROR, "页号必须是整数");

        // 查询分页结果
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVOPage);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 获取用户列表数据
     */
    @PostMapping("/get/user/list")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest getUserListRequest, HttpServletRequest request) {
        if (getUserListRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
//        final Integer userRole = getUserListRequest.getUserRole();
        User loginUser = userService.getLoginUser(request);
//        final Integer adminRole = loginUser.getUserRole();
        final String dept = loginUser.getDept();
        List<UserNameVO> userNameVO = new ArrayList<>();
        List<User> userList;
//        if (adminRole == 3) {
//            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole));
//            BeanUtils.copyProperties(userList, userNameVO);
//        } else {
//            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole).eq("dept", dept));
//        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userNameVO);
    }

    /**
     * 获取专业数据
     */
    @PostMapping("get/project/list")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        long current = projectQueryRequest.getCurrent();
        long size = projectQueryRequest.getPageSize();
        Page<Project> projectPage = projectService.page(new Page<>(current, size),
                projectService.getQueryWrapper(projectQueryRequest));
        List<ProjectVO> projectVOList = new ArrayList<>();
        for (Project project : projectPage.getRecords()) {
            final String projectName = project.getProjectName();
            final ProjectVO projectVO = new ProjectVO();
            projectVO.setLabel(projectName);
            projectVO.setValue(projectName);
            projectVOList.add(projectVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectVOList);
    }

    /**
     * 删除用户
     *
     * @param deleteDeptRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/dept")
    public BaseResponse<Boolean> deleteDept(@RequestBody DeleteDeptRequest deleteDeptRequest, HttpServletRequest request) {
        if (deleteDeptRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final String deptName = deleteDeptRequest.getDeptName();
        boolean b = deptService.remove(new QueryWrapper<Dept>().eq("deptName", deptName));
        if (!b) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, b);
    }

    /**
     * 删除专业
     *
     * @param deleteProjectRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/project")
    public BaseResponse<Boolean> deleteProject(@RequestBody DeleteProjectRequest deleteProjectRequest, HttpServletRequest request) {
        if (deleteProjectRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final String projectName = deleteProjectRequest.getProjectName();
        boolean b = projectService.remove(new QueryWrapper<Project>().eq("projectName", projectName));
        if (!b) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, b);
    }

    /**
     * 教师双选
     */
    @PostMapping("/select/student")
    public BaseResponse<String> selectStudent(@RequestBody SelectStudentRequest selectStudentRequest, HttpServletRequest request) {
        // 检查请求参数是否为空
        if (selectStudentRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "选课请求参数不能为空");
        }

        // 获取当前登录用户并检查用户角色
        User loginUser = userService.getLoginUser(request);


        // 获取选课用户账号和课题名称
        String userAccount = selectStudentRequest.getUserAccount();
        String topicName = selectStudentRequest.getTopic();

        // 根据课题名称查找课题
        Topic topic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topicName));
        if (topic == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");
        }

        Long topicId = topic.getId();

        // 同步代码块，确保课题选择的线程安全
        synchronized (topicName.intern()) {
            // 检查学生是否已经选择过课题
            StudentTopicSelection existingSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
            if (existingSelection != null) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "学生已经选择过课题");
            }
            //判断是否有余量
            final Integer surplusQuantity = topic.getSurplusQuantity();
            if (surplusQuantity <= 0) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "余量不足");
            }
            // 创建新的学生选题对象并保存
            StudentTopicSelection newSelection = new StudentTopicSelection();
            newSelection.setUserAccount(userAccount);
            newSelection.setTopicId(topicId);
            newSelection.setStatus(1);

            boolean saveSuccess = studentTopicSelectionService.save(newSelection);
            if (!saveSuccess) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "保存学生选题信息失败");
            }

            // 更新课题的剩余数量并保存
            topic.setSurplusQuantity(0);
            boolean topicSaveSuccess = topicService.saveOrUpdate(topic);
            if (!topicSaveSuccess) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "更新课题剩余数量失败");
            }

            // 返回成功信息
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "1");
        }
    }

    /**
     * 删除选题
     */
    @PostMapping("/delete/topic")
    public BaseResponse<Boolean> deleteTopic(@RequestBody DeleteTopicRequest deleteTopicRequest, HttpServletRequest request) {
        if (deleteTopicRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
//        if (userRole != 1 && userRole != 2 && userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        final Long id = deleteTopicRequest.getId();
        boolean b = topicService.removeById(id);
        final boolean b1 = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", id));
        if (!b || !b1) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, b);
    }

    /**
     * 退选
     */
    @PostMapping("/withdraw")
    public BaseResponse<Boolean> Withdraw(@RequestBody DeleteTopicRequest deleteTopicRequest, HttpServletRequest request) {
        // 检查请求参数是否为空
        if (deleteTopicRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "退选请求参数不能为空");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 获取退选的课题ID
        Long topicId = deleteTopicRequest.getId();

        // 同步代码块，确保退选操作的线程安全
        synchronized (topicId) {
            // 根据ID获取课题
            Topic topic = topicService.getById(topicId);
            if (topic == null) {
                throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");
            }

            // 更新课题的剩余数量
            topic.setSurplusQuantity(1); // 假设每次退选增加一个剩余数量
            boolean updateSuccess = topicService.updateById(topic);
            if (!updateSuccess) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "更新课题信息失败");
            }

            // 删除学生的选题记录
            boolean removeSuccess = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
            if (!removeSuccess) {
                throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "删除学生选题记录失败");
            }

            // 返回成功信息
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        }
    }

    /**
     * 添加数量
     */
    @PostMapping("add/count")
    public BaseResponse<String> addCount(@RequestBody AddCountRequest addCountRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
//        if (userRole != 1 && userRole != 2 && userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        long id = addCountRequest.getId();
        int count = addCountRequest.getCount();
        final Topic topic = topicService.getById(id);
        topic.setSurplusQuantity(topic.getSurplusQuantity() + count);
        final boolean b = topicService.updateById(topic);
        if (!b) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "数量添加成功");
    }

    /**
     * 根据题目id获取学生
     */
    @PostMapping("get/student/by/topicId")
    public BaseResponse<List<User>> getStudentByTopicId(@RequestBody GetStudentByTopicId getStudentByTopicId, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
//        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
//        if (userRole != 1 && userRole != 2 && userRole != 3) {
//            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
//        }
        if (getStudentByTopicId == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final List<StudentTopicSelection> studentList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", getStudentByTopicId.getId()));
        final ArrayList<User> userList = new ArrayList<>();
        for (StudentTopicSelection student : studentList) {
            final String userAccount = student.getUserAccount();
            final User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (user == null) {
                continue;
            }
            userList.add(user);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userList);
    }

    /**
     * 获取选题by题目id
     */
    @PostMapping("get/select/topic/by/id")
    public BaseResponse<List<User>> getSelectTopicById(@RequestBody GetSelectTopicById getSelectTopicById, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final List<StudentTopicSelection> list = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", getSelectTopicById.getId()).eq("status", 1));
        if (list == null) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        final List<User> userList = new ArrayList<>();
        for (StudentTopicSelection student : list) {
            final String userAccount = student.getUserAccount();
            final User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (user == null) {
                continue;
            }
            userList.add(user);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userList);
    }

    /**
     * 获取没选题学生
     */
    @PostMapping("get/un/select/topic/student/list")
    public BaseResponse<List<User>> getUnSelectTopicStudentList(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final String dept = loginUser.getDept();
        // 获取所有学生用户
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", 0).eq(StringUtils.isNotBlank(dept), "dept", dept));

        // 获取所有已经选题的学生
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list();

        // 将已经选题的学生账号存入一个Set
        Set<String> selectedUserAccounts = selectedList.stream()
                .map(StudentTopicSelection::getUserAccount)
                .collect(Collectors.toSet());

        // 筛选出未选题的学生
        List<User> unselectedUsers = userList.stream()
                .filter(user -> !selectedUserAccounts.contains(user.getUserAccount()))
                .collect(Collectors.toList());

        return TheResult.success(CodeBindMessageEnums.SUCCESS, unselectedUsers);
    }

    /**
     * 获取管理员获取题目
     */
    @PostMapping("get/topic/list/by/admin")
    public BaseResponse<Page<Topic>> getTopicListByAdmin(@RequestBody TopicQueryByAdminRequest topicQueryByAdminRequest, HttpServletRequest request) {
        long current = topicQueryByAdminRequest.getCurrent();
        long size = topicQueryByAdminRequest.getPageSize();
        Page<Topic> topicPage = topicService.page(new Page<>(current, size),
                topicService.getTopicQueryByAdminWrapper(topicQueryByAdminRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }

    /**
     * 获取系部教师数据to审核
     */
    @PostMapping("get/dept/teacher/by/admin")
    public BaseResponse<Page<DeptTeacherVO>> getTeacherByAdmin(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest, HttpServletRequest request) {
        if (deptTeacherQueryRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");
        }
        long current = deptTeacherQueryRequest.getCurrent();
        long size = deptTeacherQueryRequest.getPageSize();
        String sortField = deptTeacherQueryRequest.getSortField();
        String sortOrder = deptTeacherQueryRequest.getSortOrder();
        final User loginUser = userService.getLoginUser(request);
        final String dept = loginUser.getDept();

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("dept", dept);
        userQueryWrapper.eq("userRole", 1);
        userQueryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        List<User> users = userMapper.selectList(userQueryWrapper);

        // 创建返回的 Page 对象
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            final String userName = user.getUserName();

            // 查询该用户的课题列表
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.eq("teacherName", userName);
            topicQueryWrapper.eq("status", "-1");
            int count = (int) topicService.count(topicQueryWrapper);
            List<Topic> topicList = topicService.list(topicQueryWrapper);

            // 计算剩余数量和选择数量
            Integer surplusQuantity = 0;
            Integer selectAmount = 0;
            for (Topic topic : topicList) {
                surplusQuantity += topic.getSurplusQuantity();
                selectAmount += topic.getSelectAmount();
            }
            if (count != 0) {
                // 构建 DeptTeacherVO 对象
                DeptTeacherVO teacherVO = new DeptTeacherVO();
                teacherVO.setTeacherName(userName);
                teacherVO.setSurplusQuantity(surplusQuantity);
                teacherVO.setSelectAmount(selectAmount);
                teacherVO.setTopicAmount(count);
                teacherVOList.add(teacherVO);
            }
            // 添加到列表中
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(teacherVOList);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

}
