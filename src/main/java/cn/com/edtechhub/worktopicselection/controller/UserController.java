package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
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
import cn.com.edtechhub.worktopicselection.model.enums.Dept;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.model.vo.*;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.*;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
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

    // TODO: 等待去除
    @Resource
    private UserMapper userMapper;

    /**
     * 创建用户
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 参数检查
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, CodeBindMessageEnums.PARAMS_ERROR, "没有登陆无法使用该接口");

        Integer userRole = loginUser.getUserRole();
        ThrowUtils.throwIf(!Objects.equals(userRole, UserRoleEnum.ADMIN.getValue()), CodeBindMessageEnums.NO_AUTH_ERROR, "只有管理员才能动用该接口");

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
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final String userAccount = deleteRequest.getUserAccount();
        boolean b = userService.remove(new QueryWrapper<User>().eq("userAccount", userAccount));
        if (!b) {
            throw new BusinessException(CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, CodeBindMessageEnums.PARAMS_ERROR, "");
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest, request));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVOPage);
    }

    /**
     * 用户注册, 实际上本项目不存在注册新用户, 只能管理员由管理员手动导入系统, 这是因为学院系统的特殊性
     */
    @PostMapping("/register")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword userUpdatePassword) {
        if (userUpdatePassword == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        String userAccount = userUpdatePassword.getUserAccount();
        String userPassword = userUpdatePassword.getUserPassword();
        final String updatePassword = userUpdatePassword.getUpdatePassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, updatePassword)) {
            return null;
        }
        long result = userService.userUpdatePassword(userAccount, userPassword, updatePassword);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        boolean result = userService.userLogout(request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
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
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }

    /**
     * 获取用户列表数据
     */
    @PostMapping("/get/user/list")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest getUserListRequest, HttpServletRequest request) {
        if (getUserListRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final Integer userRole = getUserListRequest.getUserRole();
        User loginUser = userService.getLoginUser(request);
        final Integer adminRole = loginUser.getUserRole();
        final String dept = loginUser.getDept();
        List<UserNameVO> userNameVO = new ArrayList<>();
        List<User> userList;
        if (adminRole == 3) {
            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole));
            BeanUtils.copyProperties(userList, userNameVO);
        } else {
            userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole).eq("dept", dept));
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userNameVO);
    }

    /**
     * 获取系部数据
     */
    @PostMapping("get/dept/list")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest deptQueryRequest, HttpServletRequest request) {
        long current = deptQueryRequest.getCurrent();
        long size = deptQueryRequest.getPageSize();
        Page<Dept> deptPage = deptService.page(new Page<>(current, size),
                deptService.getDeptQueryWrapper(deptQueryRequest, request));
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

    /**
     * 获取专业数据
     */
    @PostMapping("get/project")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        long current = projectQueryRequest.getCurrent();
        long size = projectQueryRequest.getPageSize();
        Page<Project> projectPage = projectService.page(new Page<>(current, size),
                projectService.getQueryWrapper(projectQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }

    /**
     * 获取专业数据
     */
    @PostMapping("get/project/list")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest projectQueryRequest, HttpServletRequest request) {
        long current = projectQueryRequest.getCurrent();
        long size = projectQueryRequest.getPageSize();
        Page<Project> projectPage = projectService.page(new Page<>(current, size),
                projectService.getQueryWrapper(projectQueryRequest, request));
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
     * 获取系部数据
     */
    @PostMapping("get/dept")
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest deptQueryRequest, HttpServletRequest request) {
        long current = deptQueryRequest.getCurrent();
        long size = deptQueryRequest.getPageSize();
        Page<Dept> deptPage = deptService.page(new Page<>(current, size),
                deptService.getDeptQueryWrapper(deptQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptPage);
    }

    /**
     * 获取系部教师数据
     */
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
     * 添加系部
     */
    @PostMapping("/add/dept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest deptAddRequest, HttpServletRequest request) {
        if (deptAddRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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

    /**
     * 添加专业
     */
    @PostMapping("/add/project")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest projectAddRequest, HttpServletRequest request) {
        if (projectAddRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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

    /**
     * 添加选题
     */
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest addTopicRequest, HttpServletRequest request) {
        if (addTopicRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
        final Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", addTopicRequest.getTopic()));
        if (oldTopic != null) {
            return TheResult.error(CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该课题已存在");
        }
        final Topic topic = new Topic();
        BeanUtils.copyProperties(addTopicRequest, topic);
        if (userRole == 1) {
            topic.setTeacherName(loginUser.getUserName());
        }
        topic.setSurplusQuantity(addTopicRequest.getAmount());
        boolean result = topicService.save(topic);
        ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
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
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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
     * 更新选题
     */
    @PostMapping("/update/topic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest updateTopicRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }

        int current = updateTopicRequest.getCurrent();
        int pageSize = updateTopicRequest.getPageSize();
        List<UpdateTopicListRequest> updateTopicListRequests = updateTopicRequest.getUpdateTopicListRequests();
        final String teacherName = loginUser.getUserName();

        // 获取教师的分页topics
        Page<Topic> page = new Page<>(current, pageSize);
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

    /**
     * 重置密码
     */
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

    /**
     * 获取选题数据
     */
    @PostMapping("get/topic/list")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest topicQueryRequest, HttpServletRequest request) {
        long current = topicQueryRequest.getCurrent();
        long size = topicQueryRequest.getPageSize();
        Page<Topic> topicPage = topicService.page(new Page<>(current, size),
                topicService.getTopicQueryWrapper(topicQueryRequest, request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }

    /**
     * 添加数量
     */
    @PostMapping("add/count")
    public BaseResponse<String> addCount(@RequestBody AddCountRequest addCountRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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
     * 根据题目id添加开启时间和结束时间
     */
    @PostMapping("set/time/by/id")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest setTimeRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        if (userRole != 2 && userRole != 3) {
            throw new BusinessException(CodeBindMessageEnums.NO_AUTH_ERROR, "");
        }
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

    /**
     * 根据题目id进行预选题
     */
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

    /**
     * 根据题目id进行选题
     */
    @PostMapping("select/topic/by/id")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (request == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        if (selectTopicByIdRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "");
        }
        final Topic topic = topicService.getById(selectTopicByIdRequest.getId());
        final Date startTime = topic.getStartTime();
        final Date endTime = topic.getEndTime();
        final Date now = new Date();
        if (now.before(startTime) || now.after(endTime)) {
            throw new BusinessException(CodeBindMessageEnums.NOT_TIME_ERROR, "");
        }
        if (topic.getSurplusQuantity() <= 0) {
            throw new BusinessException(CodeBindMessageEnums.NOT_SURPLUS_ERROR, "");
        }
        final StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
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

    /**
     * 根据获取老师姓名
     */
    @PostMapping("get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest teacherQueryRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
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

    /**
     * 获取预选题by账号
     */
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

    /**
     * 获取选题by账号
     */
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
     * 获取选题情况
     */
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
     * 审核题目
     */
    @PostMapping("check/topic")
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
