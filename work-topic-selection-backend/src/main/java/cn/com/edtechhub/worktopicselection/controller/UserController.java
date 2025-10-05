package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.TopicConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.ai.AIManager;
import cn.com.edtechhub.worktopicselection.manager.ai.AIResult;
import cn.com.edtechhub.worktopicselection.manager.redis.RedisManager;
import cn.com.edtechhub.worktopicselection.manager.sentine.SentineManager;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeleteDeptRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.SetDeptConfigRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.DeleteProjectRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.schedule.SetTimeRequest;
import cn.com.edtechhub.worktopicselection.model.dto.schedule.UnSetTimeRequest;
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
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户控制层
 * TODO: 添加 Soket 处理推送功能, 同时需要给出提示声音, 用于 --> 教师提交审核通知主任、主任审核通过通知教师、老师选择学生通知学生、学生选择教师通知教师
 * TODO: 添加 AI 智能体阅读功能, 用于辅助教师和学生查重和寻找题目
 * TODO: 修复批量导入乱码功能
 * TODO: 修复按照当前逻辑修复选题使用教师名字的问题
 * TODO: 重构 Service 层, 该迁移就迁移
 * TODO: 重构接口层, 提高可维护性, 按道理来说, 切割接口层的影响不大, 有 IDEA 兜底
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 注入 SentineManager 依赖
     */
    @Resource
    SentineManager sentineManager;

    /**
     * 注入事务管理依赖
     */
    @Resource
    TransactionTemplate transactionTemplate;

    /**
     * 注入 Redis 管理依赖
     */
    @Resource
    RedisManager redisManager;

    /**
     * 注入 AI 管理依赖
     */
    @Resource
    private AIManager aiManager;

    /**
     * 注入用户服务依赖
     */
    @Resource
    private UserService userService;

    /**
     * 注入系部服务依赖
     */
    @Resource
    private DeptService deptService;

    /**
     * 注入专业服务依赖
     */
    @Resource
    private ProjectService projectService;

    /**
     * 注入选题服务依赖
     */
    @Resource
    private TopicService topicService;

    /**
     * 注入学生选题关联服务依赖
     */
    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    /**
     * 注入邮箱服务依赖
     */
    @Resource
    private MailService mailService;

    /**
     * 引入开关服务依赖
     */
    @Resource
    private SwitchService switchService;

    /// 测试相关接口 ///
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return TheResult.notyet();
    }

    /// 用户相关接口 ///

    /**
     * 创建用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户的学号/工号");

        String userName = request.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户名字");

        User oldUser = userService.userIsExist(userAccount, userName);
        ThrowUtils.throwIf(oldUser != null, CodeBindMessageEnums.PARAMS_ERROR, "该学号/工号对应的用户已经存在, 请不要重复添加, 如果需要更改用户信息可以先删除用户再重新添加");

        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "缺少用户角色");
        assert userRole != null;

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "本系统不存在该用户角色");

        String userDeptName = request.getDeptName();
        ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");

        // 不允许添加相同角色并且名字相同的用户
        User aUser = userService.getOne(new QueryWrapper<User>()
                .eq("userName", userName)
                .eq("userRole", userRole)
        );
        ThrowUtils.throwIf(aUser != null, CodeBindMessageEnums.PARAMS_ERROR, "不允许添加同名的用户, 请不要重复添加, 请加上数字后缀避免相同");

        // 如果有选择专业则必须选择系部所属的专业
        String userProject = request.getProject();
        if (StringUtils.isNotBlank(userProject)) {
            Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", userProject));
            ThrowUtils.throwIf(!project.getDeptName().equals(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "[" + project.getProjectName() + "] 专业属于 [" + project.getDeptName() + "] 系部, 请正确选择系部和专业");
        }

        // 如果是添加学生则需要检查是否设置了系部和专业
        if (userRoleEnum == UserRoleEnum.STUDENT) {
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
            ThrowUtils.throwIf(StringUtils.isBlank(userProject), CodeBindMessageEnums.PARAMS_ERROR, "缺少专业名称");
        }
        // 如果是添加主任或教师则需要检查是否设置了系部和专业
        else if (userRoleEnum == UserRoleEnum.DEPT || userRoleEnum == UserRoleEnum.TEACHER) {
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
        } else if (userRoleEnum == UserRoleEnum.ADMIN) {
            log.info("添加管理员");
        }
        // 兜底情况
        else {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "系统发生未知情况, 请及时联系管理员 898738804@qq.com");
        }

        // 创建新的用户实例
        return transactionTemplate.execute(transactionStatus -> {
            User user = new User();
            BeanUtils.copyProperties(request, user); // 数据库中的 userAccount 是主键
            user.setDept(userDeptName);
            user.setProject(userProject);
            user.setUserRole(userRole);
            user.setStatus(null);
            user.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes()));

            boolean result = userService.save(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "添加新的用户失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getId());
        });
    }

    /**
     * 删除用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "指定删除的用户账号不能为空");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在无需删除");
        assert user != null;

        // 删除用户
        return transactionTemplate.execute(transactionStatus -> {
            // 不允许删除超级管理员
            Long id = user.getId();
            ThrowUtils.throwIf(id == 1L, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法删除超级管理员");

            // 如果删除的是教师, 则需要把该教师关联的选题记录删除, 同时清空选题关联记录(无论是否预选)
            if (userService.userIsTeacher(user)) {
                // 先找出该教师的所有关联题目的 id
                List<Topic> topicList = topicService.list(
                        new QueryWrapper<Topic>().eq("teacherName", user.getUserName())
                );
                List<Long> topicIds = topicList
                        .stream()
                        .map(Topic::getId)
                        .collect(Collectors.toList());

                // 如果该教师确实有题目
                if (!topicIds.isEmpty()) {
                    // 删除关联的学生选题记录
                    studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().in("topicId", topicIds));
                    // 同时删除这些题目
                    topicService.removeByIds(topicIds);
                }
            }

            // 如果删除的是学生, 则需要清空选题关联记录(无论是否预选)
            if (userService.userIsStudent(user)) {
                // 查询该学生已选的所有题目
                List<StudentTopicSelection> selections = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));

                // 如果该确实有关联记录
                if (!selections.isEmpty()) {
                    // 获取所有 topicId
                    List<Long> topicIds = selections
                            .stream()
                            .map(StudentTopicSelection::getTopicId)
                            .collect(Collectors.toList());

                    // 删除所有学生选题记录
                    studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));

                    // 对应题目余量 +1
                    for (Long topicId : topicIds) {
                        Topic topic = topicService.getById(topicId);
                        if (topic != null) {
                            topic.setSurplusQuantity(topic.getSurplusQuantity() + 1); // 虽然预选人数可能会乱, 但是这个本来就是参考数值, 最重要的是剩余数量需要释放出来
                            topicService.updateById(topic);
                        }
                    }
                }
            }

            // 删除用户
            boolean result = userService.removeById(user.getId());
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "删除用户失败");

            // 删除用户所选的选题关联记录
            studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
            return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
        });
    }

    /**
     * 更新用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id < 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识不合法, 必须为正整数");

        // 创建更新后的新用户实例
        return transactionTemplate.execute(transactionStatus -> {
            User user = new User();
            BeanUtils.copyProperties(request, user);

            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 获取当前登录用户数据
     */
    @SaCheckLogin
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 获取当前登录用户
        User user = userService.userGetCurrentLoginUser();
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }

    /**
     * 获取用户分页数据
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/get/user/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取搜索条件
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(request);

        // 不是管理里情况下, 如果此时不允许跨选则不允许看到和当前登陆用户不同系部的学生
        if (!userService.userIsAdmin(userService.userGetCurrentLoginUser()) && !switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
            queryWrapper.eq("dept", userService.userGetCurrentLoginUser().getDept());
        }

        // 获取用户数据
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }

    /**
     * 获取所有教师的脱敏列表数据接口 (教师自己查主任只能获得同系部的主任)
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "用户角色不能为空");

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "该用户角色不存在");
        assert userRoleEnum != null;

        User loginUser = userService.userGetCurrentLoginUser();

        // 获取所有的教师数据（如果当前登陆用户是教师且查询的是主任就只能查询和自己同系部的主任）
        List<User> userList = userService.list(
                new QueryWrapper<User>()
                        .eq("userRole", userRoleEnum.getCode())
                        .eq(loginUser.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), "dept", loginUser.getDept())
        );
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

    /**
     * 根据 id 获取用户数据
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 返回用户信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "不存在该用户");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user);
    }

    /**
     * 根据 id 获取用户包装数据 (但是获取的是脱敏后的数据)
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @SaCheckLogin
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 获取用户数据
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();

        // 获取脱敏后的数据
        UserVO userVO = userService.getUserVO(user);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVO);
    }

    /// 认证相关接口 ///

    /**
     * 用户登入
     */
    @SaIgnore
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request, HttpServletRequest httpServletrequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的账号");

        String userPassword = request.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的密码");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在, 请发邮件向管理员确认您的账号是否未被学院导入");
        assert user != null;

        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes()); // 提前得到加密密码
        ThrowUtils.throwIf(!Objects.equals(user.getUserPassword(), encryptPassword), CodeBindMessageEnums.PARAMS_ERROR, "用户密码不正确");

        ThrowUtils.throwIf(StringUtils.isBlank(user.getStatus()), CodeBindMessageEnums.USER_INIT_PASSWD, "您的密码为系统默认密码, 请修改密码后再登陆");

        // 用户登陆
        String device = DeviceUtils.getRequestDevice(httpServletrequest); // 登陆设备
        StpUtil.login(user.getId(), device); // 开始登录
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, user); // 把用户的信息存储到 Sa-Token 的会话中, 这样后续的用权限判断就不需要一直查询 SQL 才能得到, 缺点是更新权限的时候需要把用户踢下线否则会话无法更新

        // 数据脱敏
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    /**
     * 用户注销
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 注销用户
        StpUtil.logout(); // 默认所有设备都被登出
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 用户切换登陆
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/toggle/login")
    public BaseResponse<LoginUserVO> userToggleLogin(@RequestBody UserToggleRequest request, HttpServletRequest httpServletrequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "缺少切换所需要的角色");
        assert userRole != null;
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "想要切换帐号的角色不存在");
        assert userRoleEnum != null;
        User loginUser = userService.userGetCurrentLoginUser();
        ThrowUtils.throwIf(loginUser.getUserRole().equals(userRole), CodeBindMessageEnums.PARAMS_ERROR, "当前用户已经是该角色了, 无需切换帐号");

        // 当前用户必须绑定邮箱
        ThrowUtils.throwIf(StringUtils.isBlank(loginUser.getEmail()), CodeBindMessageEnums.USER_INIT_PASSWD, "本帐号必须先绑定邮箱");

        // 然后找出可以互相切换的帐号, 这些帐号都是 不是自己、登陆过的、名字相同、密码相同、系部相同、邮箱相同(且不能为空) 一样的用户列表, 最后再根据用户需要切换的角色, 找到唯一一个满足条件的用户
        List<User> userList = userService.list(
                new QueryWrapper<User>()
                        .ne("id", loginUser.getId())
                        .eq("status", "老用户")
                        .eq("userName", loginUser.getUserName())
                        .eq("userPassword", loginUser.getUserPassword())
                        .eq("dept", loginUser.getDept())
                        .eq("email", loginUser.getEmail())
        );
        ThrowUtils.throwIf(userList.isEmpty(), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前用户无法切换角色, 需要满足两个帐号都不是初始化帐号, 且同系、同名、同密码、同邮箱才可以切换");

        // 用户切换登陆
        StpUtil.logout(loginUser.getId());
        User newUser = userList.get(0);
        String device = DeviceUtils.getRequestDevice(httpServletrequest);
        StpUtil.login(newUser.getId(), device);
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, newUser); // 把用户的信息存储到 Sa-Token 的会话中, 这样后续的用权限判断就不需要一直查询 SQL 才能得到, 缺点是更新权限的时候需要把用户踢下线否则会话无法更新

        // 数据脱敏
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(newUser, loginUserVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    /**
     * 重置用户密码
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/reset/password")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userName = request.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "用户名不能为空");

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userName", userName));
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该用户不存在, 无需重置密码");
        assert user != null;

        // 禁止对超级管理员进行操作
        ThrowUtils.throwIf(user.getUserRole().equals(UserRoleEnum.ADMIN.getCode()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "禁止对超级管理员进行该操作");

        // 获取新的初始化密码
        return transactionTemplate.execute(transactionStatus -> {
            String newEncryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
            user.setUserPassword(newEncryptPassword);
            user.setStatus(""); // 状态置新, 以方便后续强制要求用户重新登陆
            user.setEmail(""); // 同时解绑邮箱
            final boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改用户密码失败");

            // 强制要求用户重新登陆并且清理缓存
            StpUtil.logout(user.getId()); // 默认该用户的所有设备都被登出
            return TheResult.success(CodeBindMessageEnums.SUCCESS, user.getUserAccount());
        });
    }

    /**
     * 用户自己手动修改密码
     */
    @SaIgnore
    @PostMapping("/updata/password")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户学号/工号不能为空");

        String code = request.getCode();
        String userPassword = request.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(code) && StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "用户旧密码或验证码不能为空");

        String updatePassword = request.getUpdatePassword();
        ThrowUtils.throwIf(StringUtils.isBlank(updatePassword), CodeBindMessageEnums.PARAMS_ERROR, "新密码不能为空");

        // 必须通过学号或工号以及密码验证后才能修改密码
        User user;
        if (StringUtils.isNotBlank(code)) {
            // 如果使用验证码重置
            String codeInRedis = redisManager.getValue("code:" + userAccount);
            ThrowUtils.throwIf(codeInRedis == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "临时密码已过期, 请重新获取临时密码");
            assert codeInRedis != null;

            ThrowUtils.throwIf(!codeInRedis.equals(code), CodeBindMessageEnums.PARAMS_ERROR, "您的临时密码错误");

            user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            ThrowUtils.throwIf(user == null, CodeBindMessageEnums.SYSTEM_ERROR, "用户不存在");

            // 并且立刻失效
            redisManager.deleteKey("code:" + userAccount);
        } else {
            // 如果使用旧密码重置
            user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userPassword", DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes())));
            ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "旧密码不正确, 无法修改密码, 如果忘记旧密码请发送邮箱 898738804@qq.com 向联系管理员重置密码");
        }
        assert user != null;

        // 如果邮箱不为空并且用户还尚未注册绑定邮箱则需要绑定邮箱
        String email = request.getEmail();
        if (StringUtils.isNotBlank(email)) {
            ThrowUtils.throwIf(StringUtils.isNotBlank(user.getEmail()), CodeBindMessageEnums.PARAMS_ERROR, "用户已绑定邮箱, 请不要重复绑定, 解绑请联系管理员 898738804@qq.com");
            user.setEmail(email);
        }

        // 更新状态避免重复修改密码
        user.setStatus("老用户");

        // 更新用户
        User finalUser = user;
        return transactionTemplate.execute(transactionStatus -> {
            finalUser.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.SALT + updatePassword).getBytes()));
            boolean result = userService.updateById(finalUser);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改密码失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, finalUser.getId());
        });
    }

    /**
     * 发送临时密码
     */
    @SaIgnore
    @PostMapping("/send/code")
    public BaseResponse<String> sendCode(@RequestBody SendCodeRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户帐号不能为空");

        // 查找用户邮箱
        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "用户不存在");
        assert user != null;

        String email = user.getEmail();
        ThrowUtils.throwIf(StringUtils.isBlank(email), CodeBindMessageEnums.PARAMS_ERROR, "当前用户还没有绑定邮箱，如果无法修改密码请联系管理员 898738804@qq.com");

        // 如果缓存中有临时立马则直接使用, 如果没有就新缓存一个
        String code = redisManager.getValue("code:" + userAccount);
        if (StringUtils.isBlank(code)) {
            // 获取 10 位临时密码
            Random random = new Random();
            String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*";
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();

            // 缓存验证码
            redisManager.setValue("code:" + userAccount, code, 2 * 60);
        }

        try {
            mailService.sendCodeMail(email, "广州南方学院毕业设计选题系统", code);
        } catch (Exception e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "邮件发送失败, 请联系管理员 898738804@qq.com");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "发送成功, 请在您的 QQ 邮箱中查收!");
    }

    /**
     * 发送验证码
     */
    @SaIgnore
    @PostMapping("/send/captcha")
    public BaseResponse<String> sendCaptcha(@RequestBody CaptchaRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String email = request.getEmail();
        ThrowUtils.throwIf(StringUtils.isBlank(email), CodeBindMessageEnums.PARAMS_ERROR, "需要发送验证码的邮箱不能为空");

        // 如果缓存中有临时立马则直接使用, 如果没有就新缓存一个
        String code = redisManager.getValue("captcha:" + email);
        if (StringUtils.isBlank(code)) {
            // 获取 10 位临时密码
            Random random = new Random();
            String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*";
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 5; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();

            // 缓存验证码
            redisManager.setValue("captcha:" + email, code, 2 * 60);
        }

        try {
            mailService.sendCaptchaMail(email, "广州南方学院毕业设计选题系统", code);
        } catch (Exception e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "邮件发送失败, 请联系管理员 898738804@qq.com");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "发送成功, 请在您的 QQ 邮箱中查收!");
    }

    /**
     * 校验验证码
     */
    @SaIgnore
    @PostMapping("/check/captcha")
    public BaseResponse<Boolean> checkCaptcha(@RequestBody CheckCaptchaRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String email = request.getEmail();
        ThrowUtils.throwIf(StringUtils.isBlank(email), CodeBindMessageEnums.PARAMS_ERROR, "没有填写校验码, 无法确认该邮箱归您本人所属");

        String captcha = request.getCaptcha();
        ThrowUtils.throwIf(StringUtils.isBlank(captcha), CodeBindMessageEnums.PARAMS_ERROR, "没有填写校验码, 无法确认该邮箱归您本人所属");

        // 如果缓存中有临时立马则直接使用, 如果没有就新缓存一个
        String captchaInRedis = redisManager.getValue("captcha:" + email);
        ThrowUtils.throwIf(StringUtils.isBlank(captchaInRedis), CodeBindMessageEnums.NOT_FOUND_ERROR, "请先点击发送验证码");

        // 校验验证码
        ThrowUtils.throwIf(!captchaInRedis.equals(captcha), CodeBindMessageEnums.PARAMS_ERROR, "验证码错误");

        // 立刻失效
        redisManager.deleteKey("captcha:" + email);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /// 系部专业相关接口 ///

    /**
     * 添加系部
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/dept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String deptName = request.getDeptName();
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

    /**
     * 添加专业
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/project")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String projectName = request.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        String deptName = request.getDeptName();
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

    /**
     * 删除系部
     *
     * @param request 请求结构
     */
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/dept")
    public BaseResponse<Boolean> deleteDept(@RequestBody DeleteDeptRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String deptName = request.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        // 保证先删除专业才能删除系部
        List<Project> projectList = projectService.list(new QueryWrapper<Project>().eq("deptName", deptName));
        if (!projectList.isEmpty()) {
            String projectNames = projectList.stream().map(Project::getProjectName).collect(Collectors.joining(", "));
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有专业（" + projectNames + "）后才能删除该系部");
        }

        // 保证先删除相关角色才能删除系部
        List<User> userList = userService.list(new QueryWrapper<User>().eq("dept", deptName));
        if (!userList.isEmpty()) {
            String userNames = userList.stream().limit(5).map(User::getUserName).collect(Collectors.joining(", "));
            if (userList.size() > 5) {
                userNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有角色（" + userNames + "）后才能删除该系部");
        }

        // 保证先删除相关选题才能删除系部
        List<Topic> topicList = topicService.list(new QueryWrapper<Topic>().eq("deptName", deptName));
        if (!topicList.isEmpty()) {
            String topicNames = topicList.stream().limit(5).map(Topic::getTopic).collect(Collectors.joining(", "));
            if (topicList.size() > 5) {
                topicNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有选题（" + topicNames + "）后才能删除该系部");
        }

        // 删除系部
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = deptService.remove(new QueryWrapper<Dept>().eq("deptName", deptName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该系部");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 删除专业
     *
     * @param request 请求结构
     */
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/project")
    public BaseResponse<Boolean> deleteProject(@RequestBody DeleteProjectRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String projectName = request.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        // 保证先删除相关角色才能删除系部
        List<User> userList = userService.list(new QueryWrapper<User>().eq("project", projectName));
        if (!userList.isEmpty()) {
            String userNames = userList.stream().limit(5).map(User::getUserName).collect(Collectors.joining(", "));
            if (userList.size() > 5) {
                userNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该专业的所有角色（" + userNames + "）后才能删除该专业");
        }

        // 删除专业
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = projectService.remove(new QueryWrapper<Project>().eq("projectName", projectName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该专业");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 获取系部分页数据
     */
    @SaCheckLogin
    @PostMapping("/get/dept/page")
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取系部数据
        Page<Dept> deptPage = deptService.page(new Page<>(current, size), deptService.getQueryWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptPage);
    }

    /**
     * 获取系部列表数据（非管理员只能获取和当前登陆用户系部相同的系部）
     */
    @SaCheckLogin
    @PostMapping("/get/dept/list")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 获取当前用户
        User user = userService.userGetCurrentLoginUser();

        // 获取当前用户的系部
        String deptName = user.getDept();

        // 查询所有 dept 列表
        List<Dept> deptList = deptService.list(userService.userIsAdmin(user) ? null : new QueryWrapper<Dept>().eq("deptName", deptName));

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

    /**
     * 获取专业分页数据
     */
    @SaCheckLogin
    @PostMapping("/get/project/page")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }

    /**
     * 获取专业列表数据（非管理员只能获取和当前登陆用户系部相同的系部）
     */
    @SaCheckLogin
    @PostMapping("/get/project/list")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(request));
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

    /// 选题题目相关接口 ///

    /**
     * 添加选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String topicTitle = request.getTopic();
        ThrowUtils.throwIf(topicTitle == null, CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能为空");

        String topicContent = request.getDescription();
        ThrowUtils.throwIf(topicContent == null || topicContent.length() < 5, CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空, 并且不能少于 4 个字符");

        Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", request.getTopic()));
        ThrowUtils.throwIf(oldTopic != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该选题已存在, 请不要重复添加");

        // 获取当前用户
        User user = userService.userGetCurrentLoginUser();

        // 添加新的选题
        synchronized (user.getId()) { // 用教师 id 来加锁, 这样对同一个教师只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 如果教师的选题数量为 0 则不允许继续出题
                User loginUser = userService.userGetCurrentLoginUser();
                int topicAmount = loginUser.getTopicAmount();
                ThrowUtils.throwIf(topicAmount <= 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "剩余出题数量为 " + topicAmount + ", 请不要继续添加题目");

                // 添加新的题目
                Topic topic = new Topic();
                BeanUtils.copyProperties(request, topic);
                topic.setTeacherName(loginUser.getUserName());
                topic.setSurplusQuantity(request.getAmount());
                boolean result = topicService.save(topic);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的选题");

                // 更新教师的选题数量
                userService.update(new UpdateWrapper<User>().eq("id", loginUser.getId()).set("topicAmount", topicAmount - 1));
                return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
            });
        }
    }

    /**
     * 删除选题
     */
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/delete/topic")
    public BaseResponse<Boolean> deleteTopic(@RequestBody DeleteTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        assert id != null;
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 获取当前用户
        User user = userService.userGetCurrentLoginUser();

        // 删除选题的同时删除该选题对应的某位学生的最终选题关联记录
        synchronized (user.getId()) { // 用教师 id 来加锁, 这样对同一个教师只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                boolean topicRemoveResalt = topicService.removeById(id);
                studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", id)); // 直接把对应题目删除即可, 不用管是那一个学生选择了这个题目
                ThrowUtils.throwIf(!topicRemoveResalt, CodeBindMessageEnums.OPERATION_ERROR, "无法删除, 出现未知的错误");

                // 同时把教师的题目上限数量加回来
                User loginUser = userService.userGetCurrentLoginUser();
                int topicAmount = loginUser.getTopicAmount();
                userService.update(new UpdateWrapper<User>().eq("id", loginUser.getId()).set("topicAmount", topicAmount + 1));
                return TheResult.success(CodeBindMessageEnums.SUCCESS, topicRemoveResalt);
            });
        }
    }

    /**
     * 获取教师题目上限
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/teacher/topicAmount")
    public BaseResponse<Integer> getTeacherTopicAmount(@RequestBody GetTeacherTopicAmountRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long teacherId = request.getTeacherId();
        ThrowUtils.throwIf(teacherId == null || teacherId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "教师标识不合法");

        // 获取教师信息
        User teacher = userService.getById(teacherId);
        ThrowUtils.throwIf(teacher == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "教师不存在");
        assert teacher != null;
        ThrowUtils.throwIf(!teacher.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), CodeBindMessageEnums.PARAMS_ERROR, "该用户不是教师");

        // 返回教师题目上限
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacher.getTopicAmount());
    }

    /**
     * 修改教师题目上限
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/teacher/topicAmount")
    public BaseResponse<Boolean> setTeacherTopicAmount(@RequestBody SetTeacherTopicAmountRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long teacherId = request.getTeacherId();
        ThrowUtils.throwIf(teacherId == null || teacherId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "教师标识不合法");
        assert teacherId != null;

        Integer topicAmount = request.getTopicAmount();
        ThrowUtils.throwIf(topicAmount == null || topicAmount < 0 || topicAmount > 20, CodeBindMessageEnums.PARAMS_ERROR, "题目上限数量必须在 0-20 之间");
        assert topicAmount != null;

        // 获取教师信息
        User teacher = userService.getById(teacherId);
        ThrowUtils.throwIf(teacher == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "教师不存在");
        assert teacher != null;
        ThrowUtils.throwIf(!teacher.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), CodeBindMessageEnums.PARAMS_ERROR, "该用户不是教师");

        // 检查教师目前的题目数量, 如果目前的题目数量已经到上限, 则不允许改小
        long currentTopicCount = topicService.count(new QueryWrapper<Topic>().eq("teacherName", teacher.getUserName()));
        ThrowUtils.throwIf(topicAmount < currentTopicCount, CodeBindMessageEnums.PARAMS_ERROR, "不能将题目上限设置为小于当前已出题目数量(" + currentTopicCount + ")");

        // 更新教师题目上限
        synchronized (teacherId) { // 用教师 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                teacher.setTopicAmount(topicAmount);
                boolean result = userService.updateById(teacher);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新教师题目上限失败");
                return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
            });
        }
    }

    /**
     * 审核题目或重新审核题目
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/check/topic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 不能为空");
        assert id != null;
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 必须是正整数");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "选题状态不能为空");
        assert status != null;
        TopicStatusEnum statusEnum = TopicStatusEnum.getEnums(status);
        ThrowUtils.throwIf(statusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "未知的选题状态");

        String reason = request.getReason();
        ThrowUtils.throwIf(reason != null && reason.length() > TopicConstant.MAX_REASON_SIZE, CodeBindMessageEnums.PARAMS_ERROR, "理由过长, 不能超过 1024 符");

        Topic topic = topicService.getById(id);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "对应的选题不存在, 无需进行审核");
        assert topic != null;

        Integer currentStatus = topic.getStatus();
        TopicStatusEnum currentStatusEnum = TopicStatusEnum.getEnums(currentStatus);
        ThrowUtils.throwIf(currentStatusEnum == statusEnum, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "请不要重复设置相同的状态");

        // 更新选题审核内容
        return transactionTemplate.execute(transactionStatus -> {
            topic.setStatus(status);

            // 如果有审核理由则添加否则去除(并且必要时需要进行邮件通知)
            if (reason != null) {
                topic.setReason(reason);
                String name = topic.getTeacherName();
                User user = userService.getOne(new QueryWrapper<User>()
                        .eq("userName", name)
                        .eq("userRole", UserRoleEnum.TEACHER.getCode())
                );
                if (user != null && StringUtils.isNotBlank(user.getEmail())) {
                    mailService.sendReasonMail(user.getEmail(), "广州南方学院毕业设计选题系统", topic.getReason());
                }
            } else {
                topic.setReason("");
            }

            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新题目状态失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 根据题目 id 列表添加开放的开始时间和结束时间来发布选题列表
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/time/by/id")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        List<Topic> topicList = request.getTopicList();
        ThrowUtils.throwIf(topicList == null || topicList.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "请先选择要开放的题目");

        Date startTime = request.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择开始时间");
        assert startTime != null;

        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择结束时间");
        assert endTime != null;

        // 时间范围检查
        ThrowUtils.throwIf(startTime.after(endTime), CodeBindMessageEnums.PARAMS_ERROR, "开始时间不能晚于结束时间");

        // 并且时间范围均不能早于当前时间
        ThrowUtils.throwIf(startTime.before(new Date()), CodeBindMessageEnums.PARAMS_ERROR, "开始时间不能早于当前时间");
        ThrowUtils.throwIf(endTime.before(new Date()), CodeBindMessageEnums.PARAMS_ERROR, "结束时间不能早于当前时间");

        // 遍历选题列表开始设置开始时间和结束时间
        return transactionTemplate.execute(transactionStatus -> {
            for (Topic topic : request.getTopicList()) {
                topic.setStatus(TopicStatusEnum.PUBLISHED.getCode());
                topic.setStartTime(startTime);
                topic.setEndTime(endTime);
                boolean result = topicService.updateById(topic);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法开放该选题, 请联系管理员 898738804@qq.com");
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "成功开放题目!");
        });
    }

    /**
     * 根据题目 id 列表取消以及发布的选题列表, 并且置为空时间
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/unset/time/by/id")
    public BaseResponse<String> unsetTimeById(@RequestBody UnSetTimeRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        List<Topic> topicList = request.getTopicList();
        ThrowUtils.throwIf(topicList == null || topicList.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "请先选择要开放的题目");

        // 遍历选题列表开始取消开放
        return transactionTemplate.execute(transactionStatus -> {
            for (Topic topic : request.getTopicList()) {
                topic.setStatus(TopicStatusEnum.NOT_PUBLISHED.getCode());
                topic.setStartTime(new Date(0)); // 这里是无法把时间置为 null 的
                topic.setEndTime(new Date(0)); // 这里是无法把时间置为 null 的
                boolean result = topicService.updateById(topic);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法开放该选题, 请联系管理员 898738804@qq.com");
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "成功取消发布!");
        });
    }

    /**
     * 根据题目 id 进行预先选题的操作 (确认预先选题和取消预先选题)
     */
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
    @SaCheckLogin
    @PostMapping("/preselect/topic/by/id")
    public BaseResponse<Long> preSelectTopicById(@RequestBody SelectTopicByIdRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "操作状态不能为空");

        StudentTopicSelectionStatusEnum studentTopicSelectionStatusEnum = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(studentTopicSelectionStatusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "不存在这种状态");

        User loginUser = userService.userGetCurrentLoginUser();

        // 处理预选操作
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 设置题目剩余数量操作数字
                int opt = 0;

                // 确认预选题目
                if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.EN_PRESELECT) {
                    // 如果此时不允许跨选则不允许预选和当前登陆用户不同系部的选题
                    ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH) && !loginUser.getDept().equals(topic.getDeptName()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "不允许跨系部选题, 请等待开放");

                    // 不允许重复确认预选
                    long count = studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", topicId));
                    ThrowUtils.throwIf(count > 0, CodeBindMessageEnums.OPERATION_ERROR, "不能重复预选该题目");

                    // 不允许已经获取选题的人进行预选
                    int isOk = Math.toIntExact(studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())));
                    ThrowUtils.throwIf(isOk != 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经提交了题目, 不能再预选新的题目了");

                    // 限制预选数量
                    int selectedCount = Math.toIntExact(studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount())));
                    ThrowUtils.throwIf(selectedCount >= 10, CodeBindMessageEnums.OPERATION_ERROR, "最多只能预选 10 个题目");

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
                    boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", request.getId()));
                    ThrowUtils.throwIf(!remove, CodeBindMessageEnums.NOT_FOUND_ERROR, "不存在需要取消预选的题目");

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

    /**
     * 根据题目 id 进行提交选题的操作 (确认提交选题和取消提交选题)
     */
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
    @SaCheckLogin
    @PostMapping("/select/topic/by/id")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

        ThrowUtils.throwIf(!topic.getStatus().equals(TopicStatusEnum.PUBLISHED.getCode()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该题目未发布, 还不可以选中");

        Date startTime = topic.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有设置开始时间");

        Date endTime = topic.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有设置结束时间");

        Date now = new Date();
        ThrowUtils.throwIf(now.before(startTime) || now.after(endTime), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前不在选题开放范围内, 请等待管理员开放选题");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "请添加选择操作状态");

        StudentTopicSelectionStatusEnum statusEnums = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(statusEnums == null, CodeBindMessageEnums.PARAMS_ERROR, "该操作状态非法");

        // 尝试获取当前登陆学生选题关联表中的记录
        User loginUser = userService.userGetCurrentLoginUser();
        int selectedCount = Math.toIntExact(studentTopicSelectionService.count(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())));
        ThrowUtils.throwIf(selectedCount != 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "您已经提交了题目, 不能再选新的题目了, 如果需要取消则需要联系导师");

        // 尝试获取学生选题关联表中的记录
        StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", topicId));

        // 处理选题操作
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 设置题目剩余数量操作数字
                int opt = 0;

                // 确认提交选题
                if (statusEnums == StudentTopicSelectionStatusEnum.EN_SELECT) {
                    // 检查当前单选模式能否可选题
                    ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前模式为教师选择学生模式, 无法选题");

                    // 如果此时不允许跨选则不允许选中和当前登陆用户不同系部的选题
                    ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH) && !loginUser.getDept().equals(topic.getDeptName()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "不允许跨系部选题, 请等待开放");

                    ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "余量不足无法选择该题目, 请尝试选择其他题目");
                    ThrowUtils.throwIf(selection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "您还没有预选无法直接选中");

                    // 更新题目状态
                    selection.setStatus(request.getStatus());
                    boolean update = studentTopicSelectionService.updateById(selection);
                    ThrowUtils.throwIf(!update, CodeBindMessageEnums.OPERATION_ERROR, "无法提交选题, 请联系管理员 898738804@qq.com");

                    // 修改操作标志位
                    opt = -1;
                }
                // 取消提交选题
                else if (statusEnums == StudentTopicSelectionStatusEnum.UN_SELECT) {
                    boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", request.getId()));
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

    /**
     * 更新选题信息
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/update/topic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String topicName = request.getTopicName();
        ThrowUtils.throwIf(StringUtils.isBlank(topicName), CodeBindMessageEnums.PARAMS_ERROR, "需要修改题目时必须传该题目的标题");

        String type = request.getType();
        ThrowUtils.throwIf(StringUtils.isBlank(type), CodeBindMessageEnums.PARAMS_ERROR, "题目类型不能为空");

        String description = request.getDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(description), CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空");

        String requirement = request.getRequirement();
        ThrowUtils.throwIf(StringUtils.isBlank(requirement), CodeBindMessageEnums.PARAMS_ERROR, "题目要求不能为空");

        // 获取当前登陆的教师
        User loginUser = userService.userGetCurrentLoginUser();

        // 找到符合条件的题目
        Topic topic = topicService.getOne(
                new QueryWrapper<Topic>()
                        .eq("topic", topicName)
                        .eq("teacherName", loginUser.getUserName())
                        .eq("deptName", loginUser.getDept())
        );

        topic.setType(type);
        topic.setDescription(description);
        topic.setRequirement(requirement);
        topic.setStatus(TopicStatusEnum.PENDING_REVIEW.getCode());

        // 更新题目
        return transactionTemplate.execute(transactionStatus -> {
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "更新失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "更新成功");
        });
    }

    /**
     * 教师直接帮助学生确认提交题目
     */
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/select/student")
    public BaseResponse<String> selectStudent(@RequestBody SelectStudentRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 检查当前单选模式能否可预选
        ThrowUtils.throwIf(switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前模式为学生选择教师模式, 无法双选");

        // 检查请求参数是否为空
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(userAccount == null, CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        String topicName = request.getTopic();
        ThrowUtils.throwIf(topicName == null, CodeBindMessageEnums.PARAMS_ERROR, "课题名称不能为空");

        Topic topic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topicName));
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");
        assert topic != null;

        Long topicId = topic.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");
        assert topicId != null;

        // 如果课题处于正在审核状态, 则无法选择学生
        ThrowUtils.throwIf(topic.getStatus() == TopicStatusEnum.PENDING_REVIEW.getCode(), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该课题正在审核中, 暂时无法选择学生");

        // 如果课题处于还没有处于发布状态, 则无法选择学生
        ThrowUtils.throwIf(topic.getStatus() != TopicStatusEnum.PUBLISHED.getCode(), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该课题尚未发布, 暂时无法选择学生");

        // 如果当前时间没有处于发布时间段内, 则无法选择学生
        Date startTime = topic.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有设置开始时间");

        Date endTime = topic.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "没有设置结束时间");

        Date now = new Date();
        ThrowUtils.throwIf(now.before(startTime) || now.after(endTime), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前不在选题开放范围内, 请等待管理员开放选题");

        // 教师直接分配题目给学生
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 检查学生是否已经选择过课题
                StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("topicId", topicId));
                ThrowUtils.throwIf(selection != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该学生已经确定最终的题目, 请通知该学生提前退选");

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
                topic.setSurplusQuantity(topic.getSurplusQuantity() - 1);
                topic.setSelectAmount(topic.getSelectAmount() + 1);
                boolean topicSaveSuccess = topicService.saveOrUpdate(topic);
                ThrowUtils.throwIf(!topicSaveSuccess, CodeBindMessageEnums.OPERATION_ERROR, "更新课题剩余数量失败");

                // 返回成功信息
                return TheResult.success(CodeBindMessageEnums.SUCCESS, "1"); // 鬼知道他这个 1 是个什么鬼...廖吊毛写的我不敢修改, 都 tm 上线了
            });
        }
    }

    /**
     * 教师或学生直接取消提交题目
     */
    @SaCheckRole(value = {"teacher", "student"}, mode = SaMode.OR)
    @PostMapping("/withdraw")
    public BaseResponse<Boolean> withdraw(@RequestBody DeleteTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long topicId = request.getId();
        assert topicId != null;
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 退选题目
        synchronized (topicId) { // 用选题 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                // 根据 ID 获取课题
                Topic topic = topicService.getById(topicId);
                ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题, 无需退选");
                assert topic != null;

                // 更新课题的剩余数量
                ThrowUtils.throwIf(topic.getSurplusQuantity().equals(1), CodeBindMessageEnums.OPERATION_ERROR, "该课题无人选过无需退选");
                topic.setSurplusQuantity(topic.getSurplusQuantity() + 1); // 退选就增加一个剩余数量
                topic.setSelectAmount(topic.getSelectAmount() - 1); // 退选就减一个预选数量
                boolean updateSuccess = topicService.updateById(topic);
                ThrowUtils.throwIf(!updateSuccess, CodeBindMessageEnums.OPERATION_ERROR, "无法退选");

                // 先发送邮箱
                StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
                User user = userService.userIsExist(selection.getUserAccount());
                String email = user.getEmail();
                if (StringUtils.isNotBlank(email)) {
                    mailService.sendTopicMail(email, "广州南方学院毕业设计选题系统", "您被退选题目 [" + topic.getTopic() + "], " + "操作人为 " + userService.userGetCurrentLoginUser().getUserName() + ", 请前往系统查看");
                }

                // 删除学生的选题记录
                boolean removeSuccess = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
                ThrowUtils.throwIf(!removeSuccess, CodeBindMessageEnums.OPERATION_ERROR, "删除学生选题记录失败");

                // 返回成功信息
                return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
            });
        }
    }

    /**
     * 获取选择了自己题目的学生
     */
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/by/id")
    public BaseResponse<List<User>> getSelectTopicById(@RequestBody GetSelectTopicById request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 找到对应的学生题目关联记录
        List<StudentTopicSelection> list = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", request.getId()).eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()));

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

    /**
     * 获取选题分页数据
     */
    @SaCheckLogin
    // @CacheSearchOptimization(ttl = 15, modelClass = Topic.class)
    @PostMapping("/get/topic/page")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于 0");

        // 如果是学生, 则必须检查此时是否允许允许学生查看题目
        User loginUser = userService.userGetCurrentLoginUser();
        if (userService.userIsStudent(loginUser)) {
            ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH), CodeBindMessageEnums.NOT_FOUND_ERROR, "当前时间学生无法查看选题, 请等待系统开放");
        }

        // 获取查询条件
        QueryWrapper<Topic> queryWrapper = topicService.getQueryWrapper(request);
        Integer userRole = loginUser.getUserRole();
        if (userRole == 2) {
            // 如果是主任只看到本系部的选题
            queryWrapper.eq(StringUtils.isNotBlank(loginUser.getDept()), "deptName", loginUser.getDept());
        }
        if (userRole == 1) {
            // 如果是老师, 只看到自己负责的选题
            queryWrapper.eq("teacherName", loginUser.getUserName());
        }
        if (userRole == 0) {
            // 如果是学生, 看是否开启跨选, 如果此时不允许跨选则不允许看到和当前登陆用户不同系部的教师
            if (!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
                queryWrapper.eq("deptName", loginUser.getDept());
            }

            // 而且只能看到审核通过和已经发布的题目
            queryWrapper.in("status", TopicStatusEnum.NOT_PUBLISHED.getCode(), TopicStatusEnum.PUBLISHED.getCode()); // 学生只能查看已经审核通过和已经发布的选题
        }

        // 获取选题数据
        Page<Topic> topicPage = topicService.page(new Page<>(current, size), queryWrapper);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }

    /**
     * 获取当前的选题情况 (只能获取和当前登陆用户系部相同的选题)
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/situation")
    public BaseResponse<SituationVO> getSelectTopicSituation() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

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
            selectedStudents += (int) studentTopicSelectionService
                    .count(new QueryWrapper<StudentTopicSelection>()
                            .eq("userAccount", userAccount) // 获取当前用户的记录
                            .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()) // 查询状态为已选题的
                    );
        }

        // 获取未选题人数
        int unselectedStudents = totalStudents - selectedStudents;

        // 封装返回数据
        SituationVO situationVO = new SituationVO();
        situationVO.setAmount(totalStudents);
        situationVO.setSelectAmount(selectedStudents);
        situationVO.setUnselectAmount(unselectedStudents);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, situationVO);
    }

    /**
     * 获取系部教师数据
     */
    @SaCheckLogin
    // @CacheSearchOptimization(ttl = 30, modelClass = DeptTeacherVO.class)
    @PostMapping("/get/dept/teacher")
    public BaseResponse<Page<DeptTeacherVO>> getTeacher(@RequestBody DeptTeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "当前页码必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "每页大小必须大于 0");

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        String teacherName = request.getTeacherName();

        // 获取当前登陆用
        User loginUser = userService.userGetCurrentLoginUser();

        // 如果是学生, 则必须检查此时是否允许允许学生查看题目
        if (userService.userIsStudent(loginUser)) {
            ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH), CodeBindMessageEnums.NOT_FOUND_ERROR, "当前时间学生无法查看选题, 请等待系统开放");
        }

        // 查询教师列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userRole", UserRoleEnum.TEACHER.getCode()); // 是教师的

        // 如果有教师姓名搜索条件，则添加搜索条件
        if (StringUtils.isNotBlank(teacherName)) {
            userQueryWrapper.like("userName", teacherName);
        }

        // 如果此时不允许跨选则不允许看到和当前登陆用户不同系部的教师
        if (!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
            userQueryWrapper.eq("dept", loginUser.getDept());
        }

        // 添加排序条件
        if (SqlUtils.validSortField(sortField)) {
            userQueryWrapper.orderBy(true, sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        }

        List<User> users = userService.list(userQueryWrapper); // 得到所有的教师

        // 遍历教师列表来创建返回的 Page 对象, 填充每位教师的选题情况
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            // 获得教师的名字
            String userName = user.getUserName();

            // 获得教师的对应选题
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper
                    .eq("teacherName", userName)
                    .in("status", TopicStatusEnum.NOT_PUBLISHED.getCode(), TopicStatusEnum.PUBLISHED.getCode()); // 学生只能查看已经审核通过和已经发布的选题
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

        // 对教师列表进行分页处理
        int total = teacherVOList.size();
        int fromIndex = (int) ((current - 1) * size);
        int toIndex = (int) Math.min(fromIndex + size, total);

        // 确保索引不越界
        List<DeptTeacherVO> pagedTeacherVOList = new ArrayList<>();
        if (fromIndex < total) {
            pagedTeacherVOList = teacherVOList.subList(fromIndex, toIndex);
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(pagedTeacherVOList);
        teacherPage.setTotal((long) total); // 设置总记录数
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

    /**
     * 获取当前登陆账号学生的预先选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/get/preselect/topic")
    public BaseResponse<List<Topic>> getPreTopic() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 获取查询条件
        String userAccount = loginUser.getUserAccount();
        QueryWrapper<StudentTopicSelection> queryWrapper = new QueryWrapper<StudentTopicSelection>()
                .eq("userAccount", userAccount)
                .eq("status", StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode());

        // 查询对应的预先选题记录
        ThrowUtils.throwIf(userAccount == null, CodeBindMessageEnums.OPERATION_ERROR, "参数有问题");
        List<StudentTopicSelection> studentTopicSelectionList = studentTopicSelectionService.list(queryWrapper);

        ThrowUtils.throwIf(studentTopicSelectionList == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前没有预选的题目");
        assert studentTopicSelectionList != null;

        // 填充完整的返回体, 把关联对应的选题都拿到
        List<Long> topicIds = studentTopicSelectionList
                .stream()
                .map(StudentTopicSelection::getTopicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Topic> topicList = topicService.listByIds(topicIds);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    /**
     * 获取当前登陆账号学生的最终选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("get/select/topic")
    public BaseResponse<List<Topic>> getSelectTopic() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        String userAccount = loginUser.getUserAccount();

        // 查找确认最终选题的记录
        StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()));
        log.info("用户: {} 确认了自己是否有最终选题", userAccount);

        ThrowUtils.throwIf(studentTopicSelection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前用户没有确认最终的选题");
        assert studentTopicSelection != null;

        // 封装最终的返回值
        Long topicId = studentTopicSelection.getTopicId();
        Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.OPERATION_ERROR, "不存在对应的题目, 请联系管理员 898738804@qq.com");
        topicList.add(topic);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    /**
     * 获取和当前登陆用户同系的没有选题的学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept"}, mode = SaMode.OR)
    @PostMapping("/get/unselect/topic/student/list")
    public BaseResponse<List<User>> getUnSelectTopicStudentList() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        final String dept = loginUser.getDept();

        // 获取所有学生用户
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", UserRoleEnum.STUDENT.getCode()).eq(StringUtils.isNotBlank(dept), "dept", dept));

        // 获取所有已经选题的学生
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list();

        // 将已经选题的学生账号存入一个 Set
        Set<String> selectedUserAccounts = selectedList.stream().map(StudentTopicSelection::getUserAccount).collect(Collectors.toSet());

        // 筛选出未选题的学生
        List<User> unselectedUsers = userList.stream().filter(user -> !selectedUserAccounts.contains(user.getUserAccount())).collect(Collectors.toList());

        return TheResult.success(CodeBindMessageEnums.SUCCESS, unselectedUsers);
    }

    /**
     * 管理员获取题目
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/topic/list/by/admin")
    public BaseResponse<Page<Topic>> getTopicListByAdmin(@RequestBody TopicQueryByAdminRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于等于 1");

        Page<Topic> topicPage = topicService.page(new Page<>(current, size), topicService.getTopicQueryByAdminWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);

    }

    /**
     * 分页获取用户封装列表
     */
    @SaCheckLogin
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long size = request.getPageSize();
        ThrowUtils.throwIf(size > 20, CodeBindMessageEnums.PARAMS_ERROR, "不能一次性获取过多的分页数据");

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 0, CodeBindMessageEnums.PARAMS_ERROR, "页号必须是整数");

        // 查询分页结果
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(request));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVOPage);
    }

    /**
     * 获取用户列表数据
     */
    @SaCheckLogin
    @PostMapping("/get/user/list")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Integer userRole = request.getUserRole();
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
            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole).eq("dept", dept));
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userNameVO);
    }

    /**
     * 添加数量
     */
    @SaCheckLogin
    @PostMapping("/add/count")
    public BaseResponse<String> addCount(@RequestBody AddCountRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        long id = request.getId();
        int count = request.getCount();
        final Topic topic = topicService.getById(id);
        topic.setSurplusQuantity(topic.getSurplusQuantity() + count);
        final boolean resalt = topicService.updateById(topic);
        ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.OPERATION_ERROR, "无法更新");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "数量添加成功");
    }

    /**
     * 根据题目 id 获取学生
     */
    @SaCheckLogin
    @PostMapping("/get/student/by/topicId")
    public BaseResponse<List<User>> getStudentByTopicId(@RequestBody GetStudentByTopicId request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        List<StudentTopicSelection> studentList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", request.getId()));
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

    /**
     * 获取系部教师数据 to 审核
     */
    @SaCheckLogin
    @PostMapping("/get/dept/teacher/by/admin")
    public BaseResponse<Page<DeptTeacherVO>> getTeacherByAdmin(@RequestBody DeptTeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须大于等于 1");

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        User loginUser = userService.userGetCurrentLoginUser();

        String dept = loginUser.getDept();

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("dept", dept).eq("userRole", UserRoleEnum.TEACHER).orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
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

        // 对教师列表进行分页处理
        int total = teacherVOList.size();
        int fromIndex = (int) ((current - 1) * size);
        int toIndex = (int) Math.min(fromIndex + size, total);

        // 确保索引不越界
        List<DeptTeacherVO> pagedTeacherVOList = new ArrayList<>();
        if (fromIndex < total) {
            pagedTeacherVOList = teacherVOList.subList(fromIndex, toIndex);
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(pagedTeacherVOList);
        teacherPage.setTotal((long) total); // 设置总记录数
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

    /**
     * 获取题目审核等级
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "teacher"}, mode = SaMode.OR)
    @PostMapping("/get/topic/review_level")
    public BaseResponse<AIResult> getTopicReviewLevel(@RequestBody GetTopicReviewLevelRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String topicTitle = request.getTopic();
        ThrowUtils.throwIf(topicTitle == null, CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能为空");

        String topicContent = request.getDescription();
        ThrowUtils.throwIf(topicContent == null || topicContent.length() < 5, CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空, 并且不能少于 4 个字符");

        // 获取当前登陆用户的 id 并且转化为 UUID
        Long id = userService.userGetCurrentLoginUser().getId();
        String factor = "work-topic-selection-backend"; // 调整因子，可以是随机值、时间戳、业务常量
        String raw = id + "-" + factor;
        String userId = UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)).toString();

        AIResult aiResult = aiManager.sendAi(userId, topicTitle + "在题库中有相似的题目存在么？给出判断相似程度的 JSON 响应");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, aiResult);
    }

    /**
     * 查询是否允许跨系状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/cross_topic")
    public BaseResponse<Boolean> getCrossTopicStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH));
    }

    /**
     * 设置是否允许跨系开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/cross_topic")
    public BaseResponse<String> setCrossTopicStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        switchService.setEnabled(TopicConstant.CROSS_TOPIC_SWITCH, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "跨系选题功能已" + (enabled ? "开启" : "关闭"));
    }

    /**
     * 查询学生查看选题状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/view_topic")
    public BaseResponse<Boolean> getViewTopicStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH));
    }

    /**
     * 设置学生查看选题开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/view_topic")
    public BaseResponse<String> setViewTopicStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        switchService.setEnabled(TopicConstant.VIEW_TOPIC_SWITCH, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, (enabled ? "允许" : "禁止") + "学生查看选题");
    }

    /**
     * 查询单选模式切换状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/switch_single_choice")
    public BaseResponse<Boolean> getSwitchSingleChoiceStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE));
    }

    /**
     * 设置单选模式切换开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/switch_single_choice")
    public BaseResponse<String> setSwitchSingleChoiceStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        switchService.setEnabled(TopicConstant.SWITCH_SINGLE_CHOICE, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "当前单选模式切换为" + (enabled ? "学生单选模式" : "教师单选模式"));
    }

    /**
     * 查看系部选跨选配置
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get/dept/config")
    public BaseResponse<DeptConfigVO> getDeptConfig() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        DeptConfigVO deptConfigVO = new DeptConfigVO();
        if (switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
            Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
            if (keys != null && !keys.isEmpty()) {
                Map<String, List<String>> enableSelectDeptsList = new HashMap<>();

                for (String key : keys) {
                    String value = redisManager.getValue(key);
                    if (value != null) {
                        // key 格式为 DEPT_CROSS_TOPIC_CONFIG:<deptId>
                        String objectDeptName = key.split(":")[1];

                        // value 存储的是 JSON 字符串，需要反序列化
                        List<String> enableSelectDepts = JSONUtil.toList(value, String.class);

                        enableSelectDeptsList.put(objectDeptName, enableSelectDepts);
                    }
                }

                deptConfigVO.setEnableSelectDeptsList(enableSelectDeptsList);
            }
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptConfigVO);
    }

    /**
     * 设置系部选跨选配置(request.enableSelectDeptsList 传递空 [] 则清空所有配置)
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/dept/config")
    public BaseResponse<Boolean> setDeptConfig(@RequestBody SetDeptConfigRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        // 取出没有开启跨系开关的情况
        ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH), CodeBindMessageEnums.PARAMS_ERROR, "请先开启跨系开关后再配置选题规则");

        // 清空配置
        Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
        redisManager.deleteKeys(keys);

        // 如果传递空参数就清空配置
        Map<String, List<String>> enableSelectDeptsList = request.getEnableSelectDeptsList();
        assert enableSelectDeptsList != null;
        if (enableSelectDeptsList == null && enableSelectDeptsList.isEmpty()) {
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        }

        // 重新配置
        for (Map.Entry<String, List<String>> entry : enableSelectDeptsList.entrySet()) {
            String objectDeptName = entry.getKey();
            List<String> enableSelectDepts = entry.getValue();
            redisManager.setValue(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":" + objectDeptName, JSONUtil.toJsonStr(enableSelectDepts));
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 查看系统的相关信息面板信息
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get/system/info")
    public BaseResponse<TheSystemInfoVO> getSystemInfo() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 创建存储系统信息的 VO 对象
        TheSystemInfoVO theSystemInfoVo = new TheSystemInfoVO();

        // 选题信息
        theSystemInfoVo.setTotalDeptCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 2)
                .count()
        );

        theSystemInfoVo.setTotalTeacherCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 1)
                .count()
        );

        theSystemInfoVo.setTotalStudentCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 0)
                .count()
        );

        theSystemInfoVo.setLoginUserCount(userService
                .lambdaQuery()
                .eq(User::getStatus, "老用户")
                .count()
        );

        theSystemInfoVo.setAuditPassTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, 0)
                .count()
        );

        theSystemInfoVo.setAuditBackTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, -2)
                .count()
        );

        theSystemInfoVo.setAuditTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, -1)
                .count()
        );

        theSystemInfoVo.setReleaseTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, 1)
                .count()
        );

        // 系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

        double cpuLoad = sunOsBean.getSystemCpuLoad() * 100;
        theSystemInfoVo.setCpuUsage(new DecimalFormat("0.00").format(cpuLoad) + "%");

        long totalPhysical = sunOsBean.getTotalPhysicalMemorySize();
        long freePhysical = sunOsBean.getFreePhysicalMemorySize();
        long usedPhysical = totalPhysical - freePhysical;
        theSystemInfoVo.setMemoryUsage(formatSize(usedPhysical) + "/" + formatSize(totalPhysical));

        File root = new File("/");
        long totalDisk = root.getTotalSpace();
        long freeDisk = root.getFreeSpace();
        long usedDisk = totalDisk - freeDisk;
        theSystemInfoVo.setDiskUsage(formatSize(usedDisk) + "/" + formatSize(totalDisk));

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedHeap = heapUsage.getUsed();
        long maxHeap = heapUsage.getMax();
        theSystemInfoVo.setJvmMemoryUsage(formatSize(usedHeap) + "/" + formatSize(maxHeap));

        return TheResult.success(CodeBindMessageEnums.SUCCESS, theSystemInfoVo);
    }

    /**
     * 格式化数据单位
     */
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }

    /**
     * 判断该系部的学生在跨选配置中是否允许跨选
     */
    private Boolean isStudentAllowedCrossSelect(String userDeptName, String objDeptName) {
        ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "用户系部不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(objDeptName), CodeBindMessageEnums.PARAMS_ERROR, "目标系部不能为空");

        // 如果不存在配置就默认允许全部跨选
        Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
        if (keys == null || keys.isEmpty()) {
            return true;
        }

        // 如果有存在配置就默认按照规则跨选
        String value = redisManager.getValue(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":" + userDeptName);
        if (value == null) {
            return true;
        }
        List<String> enableSelectDepts = JSONUtil.toList(value, String.class);
        return enableSelectDepts.contains(objDeptName);
    }

}
