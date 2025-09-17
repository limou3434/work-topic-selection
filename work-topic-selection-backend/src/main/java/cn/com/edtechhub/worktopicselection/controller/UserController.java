package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.annotation.CacheSearchOptimization;
import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.TopicConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
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
import cn.com.edtechhub.worktopicselection.model.entity.*;
import cn.com.edtechhub.worktopicselection.model.enums.StudentTopicSelectionStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.TopicStatusEnum;
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
import org.springframework.transaction.support.TransactionTemplate;
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
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 注入事务管理依赖
     */
    @Resource
    TransactionTemplate transactionTemplate;

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

    // 创建用户
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
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
        return transactionTemplate.execute(transactionStatus -> {
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
        });
    }

    // 删除用户
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        // 参数检查
        ThrowUtils.throwIf(deleteRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = deleteRequest.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在无需删除");

        // 删除用户
        return transactionTemplate.execute(transactionStatus -> {
            boolean result = userService.removeById(user.getId());
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "删除用户失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
        });
    }

    // 更新用户
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 检查参数
        ThrowUtils.throwIf(userUpdateRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(id < 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识不合法, 必须为正整数");

        // 创建更新后的新用户实例
        return transactionTemplate.execute(transactionStatus -> {
            User user = new User();
            BeanUtils.copyProperties(userUpdateRequest, user);
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    // 获取当前登录用户数据
    @SaCheckLogin
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        Long LoginUserId = userService.userGetCurrentLonginUserId();
        User user = userService.userGetSessionById(LoginUserId);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }

    // 获取用户分页数据
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

        // 过滤 id 为 1-56 的用户（这些都是测试用户）
        if (userService.userGetCurrentLoginUser().getId() != 1) {
            userPage.getRecords().removeIf(user -> user.getId() <= 56);
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }

    // 获取所有教师的脱敏列表数据
    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    @SaCheckLogin
    @PostMapping("get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest teacherQueryRequest) {
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

    // 根据 id 获取用户数据
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

    // 根据 id 获取用户包装数据
    @SaCheckLogin
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        // 检查参数
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 获取用户数据
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();

        // 获取脱敏后的数据
        UserVO userVO = userService.getUserVO(user);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVO);
    }

    /// 认证相关接口 ///

    // 用户登入
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
    @SaCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 注销用户
        StpUtil.logout(); // 默认所有设备都被登出
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    // 重置用户密码
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/reset/password")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
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
        return transactionTemplate.execute(transactionStatus -> {
            String newEncryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
            user.setUserPassword(newEncryptPassword);
            user.setStatus(null); // 状态置空, 以方便后续强制要求用户重新登陆
            final boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改用户密码失败");

            // 强制要求用户重新登陆并且清理缓存
            StpUtil.logout(user.getId()); // 默认该用户的所有设备都被登出
            return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getUserAccount());
        });
    }

    // 用户自己手动修改密码
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
        return transactionTemplate.execute(transactionStatus -> {
            user.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.SALT + updatePassword).getBytes()));
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改密码失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getId());
        });
    }

    /// 系部专业相关接口 ///

    // 添加系部
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
        return transactionTemplate.execute(transactionStatus -> {
            Dept newDept = new Dept();
            newDept.setDeptName(deptName);
            boolean result = deptService.save(newDept);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的系部");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, newDept.getId());
        });
    }

    // 添加专业
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
        return transactionTemplate.execute(transactionStatus -> {
            Project newProject = new Project();
            newProject.setProjectName(projectName);
            newProject.setDeptName(deptName);
            boolean result = projectService.save(newProject);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的专业");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, newProject.getId());
        });
    }

    // 删除系部
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/dept")
    public BaseResponse<Boolean> deleteDept(@RequestBody DeleteDeptRequest deleteDeptRequest) {
        // 检查参数
        ThrowUtils.throwIf(deleteDeptRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String deptName = deleteDeptRequest.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        // 删除系部
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = deptService.remove(new QueryWrapper<Dept>().eq("deptName", deptName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该系部");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    // 删除专业
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/project")
    public BaseResponse<Boolean> deleteProject(@RequestBody DeleteProjectRequest deleteProjectRequest) {
        // 检查参数
        ThrowUtils.throwIf(deleteProjectRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String projectName = deleteProjectRequest.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        // 删除专业
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = projectService.remove(new QueryWrapper<Project>().eq("projectName", projectName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该专业");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    // 获取系部分页数据
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
    @SaCheckLogin
    @PostMapping("/get/project/page")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest projectQueryRequest) {
        // 检查参数
        long current = projectQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = projectQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(projectQueryRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }

    // 获取专业列表数据
    @SaCheckLogin
    @PostMapping("/get/project/list")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest projectQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(projectQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = projectQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = projectQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(projectQueryRequest));
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

    /// 选题相关接口 ///

    // 添加选题
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest addTopicRequest) {
        // 检查参数
        ThrowUtils.throwIf(addTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", addTopicRequest.getTopic()));
        ThrowUtils.throwIf(oldTopic != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该选题已存在, 请不要重复添加");

        // 添加新的选题
        return transactionTemplate.execute(transactionStatus -> {
            User loginUser = userService.userGetCurrentLoginUser();
            Topic topic = new Topic();
            BeanUtils.copyProperties(addTopicRequest, topic);
            topic.setTeacherName(loginUser.getUserName());
            topic.setSurplusQuantity(addTopicRequest.getAmount());
            boolean result = topicService.save(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的选题");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
        });
    }

    // 删除选题
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/delete/topic")
    public BaseResponse<Boolean> deleteTopic(@RequestBody DeleteTopicRequest deleteTopicRequest) {
        // 检查参数
        ThrowUtils.throwIf(deleteTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long id = deleteTopicRequest.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");

        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 删除选题的同时删除该选题对应的某位学生的最终选题关联记录
        return transactionTemplate.execute(transactionStatus -> {
            boolean topicRemoveResalt = topicService.removeById(id);
            studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", id)); // 直接把对应题目删除即可, 不用管是那一个学生选择了这个题目
            ThrowUtils.throwIf(!topicRemoveResalt, CodeBindMessageEnums.OPERATION_ERROR, "无法删除, 出现未知的错误");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, topicRemoveResalt);
        });
    }

    // 审核题目
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/check/topic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest checkTopicRequest) {
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
        return transactionTemplate.execute(transactionStatus -> {
            topic.setStatus(status);
            if (reason != null) {
                topic.setReason(reason);
            }
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新题目状态失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    // 根据题目 id 添加开放的开始时间和结束时间
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
        return transactionTemplate.execute(transactionStatus -> {
            for (Topic topic : setTimeRequest.getTopicList()) {
                topic.setStatus(TopicStatusEnum.PUBLISHED.getCode());
                topic.setStartTime(startTime);
                topic.setEndTime(endTime);
                boolean result = topicService.updateById(topic);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法开放该选题, 请联系管理员 898738804@qq.com");
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "开始选题!");
        });
    }

    // 根据题目 id 进行预先选题的操作(确认预先选题和取消预先选题)
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
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

        // 获取当前登录用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 处理预选操作
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 设置题目剩余数量操作数字
                int opt = 0;

                // 确认预选题目
                if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.EN_PRESELECT) {
                    // 不允许重复确认预选
                    long count = studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>()
                            .eq("userAccount", loginUser.getUserAccount())
                            .eq("topicId", topicId));
                    ThrowUtils.throwIf(count > 0, CodeBindMessageEnums.OPERATION_ERROR, "不能重复预选该题目");

                    // 不允许已经获取选题的人进行预选
                    int isOk = Math.toIntExact(studentTopicSelectionService.count(
                                    new QueryWrapper<StudentTopicSelection>()
                                            .eq("userAccount", loginUser.getUserAccount())
                                            .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
                            )
                    );
                    ThrowUtils.throwIf(isOk != 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经提交了题目, 不能再预选新的题目了");

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
                }
                // 取消预选题目
                else if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.UN_PRESELECT) {
                    // 修改关联记录
                    boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
                    if (!remove) {
                        throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
                    }

                    // 修改操作标志位
                    opt = -1;
                }
                // 其他意外情况
                else {
                    ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "该接口不允许传递这种状态, 请联系管理员 898738804@qq.com");
                }

                // 处理剩余数量
                topic.setSelectAmount(topic.getSelectAmount() + opt);
                boolean updateTopic = topicService.updateById(topic);
                ThrowUtils.throwIf(!updateTopic, CodeBindMessageEnums.OPERATION_ERROR, "无法保存余量, 请联系管理员 898738804@qq.com");
                return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
            });
        }
    }

    // 根据题目 id 进行提交选题的操作(确认提交选题和取消提交选题)
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
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

        // 尝试获取当前登陆学生选题关联表中的记录
        User loginUser = userService.userGetCurrentLoginUser();
        int selectedCount = Math.toIntExact(studentTopicSelectionService.count(
                        new QueryWrapper<StudentTopicSelection>()
                                .eq("userAccount", loginUser.getUserAccount())
                                .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
                )
        );
        ThrowUtils.throwIf(selectedCount != 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经提交了题目, 不能再选新的题目了, 如果需要取消则需要联系导师");

        // 尝试获取学生选题关联表中的记录
        StudentTopicSelection selection = studentTopicSelectionService.getOne(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("userAccount", loginUser.getUserAccount())
                        .eq("topicId", topicId)
        );

        // 处理选题操作
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 设置题目剩余数量操作数字
                int opt = 0;

                // 确认提交选题
                if (statusEnums == StudentTopicSelectionStatusEnum.EN_SELECT) {
                    ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "余量不足无法选择该题目, 请尝试选择其他题目");
                    ThrowUtils.throwIf(selection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "您还没有预选无法直接选中");

                    // 更新题目状态
                    selection.setStatus(selectTopicByIdRequest.getStatus());
                    boolean update = studentTopicSelectionService.updateById(selection);
                    ThrowUtils.throwIf(!update, CodeBindMessageEnums.OPERATION_ERROR, "无法提交选题, 请联系管理员 898738804@qq.com");

                    // 同时清理所有预选题目
                    List<StudentTopicSelection> preselectList = studentTopicSelectionService.list(
                            new QueryWrapper<StudentTopicSelection>()
                                    .eq("userAccount", loginUser.getUserAccount())
                                    .eq("status", StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode())
                    );

                    for (StudentTopicSelection selectionItem : preselectList) {
                        // 遍历每一条预选记录，对应题目的 selectAmount -1
                        Long theTopicId = selectionItem.getTopicId();
                        Topic theTopic = topicService.getById(theTopicId);
                        if (theTopic != null && theTopic.getSelectAmount() > 0) {
                            theTopic.setSelectAmount(theTopic.getSelectAmount() - 1);
                            topicService.updateById(theTopic);
                        }
                    }

                    List<Long> ids = preselectList.stream().map(StudentTopicSelection::getId).collect(Collectors.toList());
                    boolean removed = studentTopicSelectionService.removeBatchByIds(ids);
                    ThrowUtils.throwIf(!removed, CodeBindMessageEnums.OPERATION_ERROR, "无法清除当前登陆用户的所有预选题目");

                    // 修改操作标志位
                    opt = -1;
                }
                // 取消提交选题
                else if (statusEnums == StudentTopicSelectionStatusEnum.UN_SELECT) {
                    boolean remove = studentTopicSelectionService.remove(
                            new QueryWrapper<StudentTopicSelection>()
                                    .eq("userAccount", loginUser.getUserAccount())
                                    .eq("topicId", selectTopicByIdRequest.getId())
                    );
                    ThrowUtils.throwIf(!remove, CodeBindMessageEnums.NOT_FOUND_ERROR, "您还没有选择题目, 无法取消选择");

                    // 修改操作标志位
                    opt = +1;
                }
                // 其他意外情况
                else {
                    ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "该接口不允许传递这种状态, 请联系管理员 898738804@qq.com");
                }

                // 更新选题
                topic.setSurplusQuantity(topic.getSurplusQuantity() + opt);
                boolean updateTopic = topicService.updateById(topic);
                ThrowUtils.throwIf(!updateTopic, CodeBindMessageEnums.OPERATION_ERROR, "无法更新选题, 请联系管理员");
                return TheResult.success(CodeBindMessageEnums.SUCCESS, selection.getId());
            });
        }
    }

    // 更新选题信息
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

        return transactionTemplate.execute(transactionStatus -> {
            // 删除 topicsToRemove 中的所有项
            if (!topicsToRemove.isEmpty()) {
                List<Long> topicsToRemoveIds = topicsToRemove.stream()
                        .map(Topic::getId)
                        .collect(Collectors.toList());
                topicService.removeByIds(topicsToRemoveIds);
                // 删除与这些主题相关的学生选择记录
                for (Long topicId : topicsToRemoveIds) {
                    studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
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
        });
    }

    // 教师直接帮助学生确认提交题目
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/select/student")
    public BaseResponse<String> selectStudent(@RequestBody SelectStudentRequest selectStudentRequest) {
        // 检查请求参数是否为空
        ThrowUtils.throwIf(selectStudentRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        String userAccount = selectStudentRequest.getUserAccount();
        ThrowUtils.throwIf(userAccount == null, CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        String topicName = selectStudentRequest.getTopic();
        ThrowUtils.throwIf(topicName == null, CodeBindMessageEnums.PARAMS_ERROR, "课题名称不能为空");

        Topic topic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topicName));
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");

        Long topicId = topic.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");

        // 教师直接分配题目给学生
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 检查学生是否已经选择过课题
                StudentTopicSelection selection = studentTopicSelectionService.getOne(
                        new QueryWrapper<StudentTopicSelection>()
                                .eq("userAccount", userAccount)
                                .eq("topicId", topicId)
                );
                ThrowUtils.throwIf(selection != null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该学生已经选择过改课题, 请通知该同学退选已经确认的题目");

                // 判断是否有余量
                Integer surplusQuantity = topic.getSurplusQuantity();
                ThrowUtils.throwIf(surplusQuantity <= 0, CodeBindMessageEnums.OPERATION_ERROR, "余量不足");

                // 创建新的学生选题对象并保存
                StudentTopicSelection newSelection = new StudentTopicSelection();
                newSelection.setUserAccount(userAccount);
                newSelection.setTopicId(topicId);
                newSelection.setStatus(StudentTopicSelectionStatusEnum.EN_SELECT.getCode());
                boolean saveSuccess = studentTopicSelectionService.save(newSelection);
                ThrowUtils.throwIf(!saveSuccess, CodeBindMessageEnums.OPERATION_ERROR, "保存学生选题信息失败");

                // 更新课题的剩余数量并保存
                topic.setSurplusQuantity(0);
                boolean topicSaveSuccess = topicService.saveOrUpdate(topic);
                ThrowUtils.throwIf(!topicSaveSuccess, CodeBindMessageEnums.OPERATION_ERROR, "更新课题剩余数量失败");

                // 返回成功信息
                return TheResult.success(CodeBindMessageEnums.SUCCESS, "1"); // TODO: 鬼知道他这个 1 是个什么鬼...
            });
        }
    }

    // 教师或学生直接生取消提交题目
    @SaCheckRole(value = {"teacher", "student"}, mode = SaMode.OR)
    @PostMapping("/withdraw")
    public BaseResponse<Boolean> withdraw(@RequestBody DeleteTopicRequest deleteTopicRequest) {
        // 检查参数
        ThrowUtils.throwIf(deleteTopicRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = deleteTopicRequest.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 退选题目
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 根据 ID 获取课题
                Topic topic = topicService.getById(topicId);
                ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题, 无需退选");

                // 更新课题的剩余数量
                ThrowUtils.throwIf(topic.getSurplusQuantity().equals(1), CodeBindMessageEnums.OPERATION_ERROR, "该课题无人选过无需退选");
                topic.setSurplusQuantity(topic.getSurplusQuantity() + 1); // 退选就增加一个剩余数量
                topic.setSelectAmount(topic.getSelectAmount() - 1); // 退选就减一个预选数量
                boolean updateSuccess = topicService.updateById(topic);
                ThrowUtils.throwIf(!updateSuccess, CodeBindMessageEnums.OPERATION_ERROR, "无法退选");

                // 删除学生的选题记录
                boolean removeSuccess = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
                ThrowUtils.throwIf(!removeSuccess, CodeBindMessageEnums.OPERATION_ERROR, "删除学生选题记录失败");

                // 返回成功信息
                return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
            });
        }
    }

    // 获取选择了自己题目的学生
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/by/id")
    public BaseResponse<List<User>> getSelectTopicById(@RequestBody GetSelectTopicById getSelectTopicById) {
        // 检查参数
        ThrowUtils.throwIf(getSelectTopicById == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long id = getSelectTopicById.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 找到对应的学生题目关联记录
        List<StudentTopicSelection> list = studentTopicSelectionService.list(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("topicId", getSelectTopicById.getId())
                        .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
        );

        // 获取用户数据
        List<User> userList = new ArrayList<>();
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

    // 获取选题分页数据
    @SaCheckLogin
    @CacheSearchOptimization(ttl = 15, modelClass = Topic.class)
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

    // 获取当前的选题情况(只能获取和当前登陆用户系部相同的选题)
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

    // 获取系部教师数据
    @SaCheckLogin
    @CacheSearchOptimization(ttl = 30, modelClass = DeptTeacherVO.class)
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
        List<User> users = userService.list(userQueryWrapper); // 得到所有的教师

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

    // 获取当前登陆账号学生的预先选题
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

    // 获取和当前登陆用户同系的没有选题的学生
    @SaCheckRole(value = {"dept"}, mode = SaMode.OR)
    @PostMapping("/get/unselect/topic/student/list")
    public BaseResponse<List<User>> getUnSelectTopicStudentList() {
        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        final String dept = loginUser.getDept();

        // 获取所有学生用户
        final List<User> userList = userService.list(
                new QueryWrapper<User>()
                        .eq("userRole", UserRoleEnum.STUDENT.getCode())
                        .eq(StringUtils.isNotBlank(dept), "dept", dept)
        );

        // 获取所有已经选题的学生
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list();

        // 将已经选题的学生账号存入一个 Set
        Set<String> selectedUserAccounts = selectedList.stream()
                .map(StudentTopicSelection::getUserAccount)
                .collect(Collectors.toSet());

        // 筛选出未选题的学生
        List<User> unselectedUsers = userList
                .stream()
                .filter(user -> !selectedUserAccounts.contains(user.getUserAccount()))
                .collect(Collectors.toList());

        return TheResult.success(CodeBindMessageEnums.SUCCESS, unselectedUsers);
    }

    // 获取管理员获取题目
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/topic/list/by/admin")
    public BaseResponse<Page<Topic>> getTopicListByAdmin(@RequestBody TopicQueryByAdminRequest topicQueryByAdminRequest) {
        // 参数检查
        ThrowUtils.throwIf(topicQueryByAdminRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = topicQueryByAdminRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = topicQueryByAdminRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于等于 1");

        Page<Topic> topicPage = topicService.page(new Page<>(current, size), topicService.getTopicQueryByAdminWrapper(topicQueryByAdminRequest));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);

    }

    // 分页获取用户封装列表
    @SaCheckLogin
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
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

    // 获取用户列表数据
    @SaCheckLogin
    @PostMapping("/get/user/list")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest getUserListRequest) {
        if (getUserListRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        Integer userRole = getUserListRequest.getUserRole();
        User loginUser = userService.userGetCurrentLoginUser();
        Integer adminRole = loginUser.getUserRole();
        String dept = loginUser.getDept();
        List<UserNameVO> userNameVO = new ArrayList<>();
        List<User> userList = new ArrayList<>();

        // 查找用户数据
        if (adminRole == UserRoleEnum.ADMIN.getCode()) {
            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole));
            BeanUtils.copyProperties(userList, userNameVO);
        } else {
            userList = userService.list(new QueryWrapper<User>()
                    .eq("userRole", userRole)
                    .eq("dept", dept)
            );
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userNameVO);
    }

    // 添加数量
    @SaCheckLogin
    @PostMapping("/add/count")
    public BaseResponse<String> addCount(@RequestBody AddCountRequest addCountRequest) {
        long id = addCountRequest.getId();
        int count = addCountRequest.getCount();
        final Topic topic = topicService.getById(id);
        topic.setSurplusQuantity(topic.getSurplusQuantity() + count);
        final boolean resalt = topicService.updateById(topic);
        ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.OPERATION_ERROR, "无法更新");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "数量添加成功");
    }

    // 根据题目 id 获取学生
    @SaCheckLogin
    @PostMapping("/get/student/by/topicId")
    public BaseResponse<List<User>> getStudentByTopicId(@RequestBody GetStudentByTopicId getStudentByTopicId) {
        // 参数检查
        ThrowUtils.throwIf(getStudentByTopicId == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        List<StudentTopicSelection> studentList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", getStudentByTopicId.getId()));
        ArrayList<User> userList = new ArrayList<>();
        for (StudentTopicSelection student : studentList) {
            String userAccount = student.getUserAccount();
            User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (user == null) {
                continue;
            }
            userList.add(user);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userList);
    }

    // 获取系部教师数据 to 审核
    @SaCheckLogin
    @PostMapping("/get/dept/teacher/by/admin")
    public BaseResponse<Page<DeptTeacherVO>> getTeacherByAdmin(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest) {
        // 检查参数
        ThrowUtils.throwIf(deptTeacherQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        long current = deptTeacherQueryRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = deptTeacherQueryRequest.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于等于 1");

        String sortField = deptTeacherQueryRequest.getSortField();
        String sortOrder = deptTeacherQueryRequest.getSortOrder();

        User loginUser = userService.userGetCurrentLoginUser();

        String dept = loginUser.getDept();

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper
                .eq("dept", dept)
                .eq("userRole", UserRoleEnum.TEACHER)
                .orderBy(
                        SqlUtils.validSortField(sortField),
                        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                        sortField
                );
        List<User> users = this.userService.list(userQueryWrapper);

        // 创建返回的 Page 对象
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            String userName = user.getUserName();

            // 查询该用户的课题列表
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.eq("teacherName", userName);
            topicQueryWrapper.eq("status", TopicStatusEnum.PENDING_REVIEW.getCode());
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

    // TODO: 添加两个原子事务接口

    // 查看指定教师的选题上限

    // 设置指定教师的选题上限

}
