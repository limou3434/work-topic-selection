package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.common.BaseResponse;
import cn.com.edtechhub.worktopicselection.common.ErrorCode;
import cn.com.edtechhub.worktopicselection.common.ResultUtils;
import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.ThrowUtils;
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
import cn.com.edtechhub.worktopicselection.model.vo.*;
import cn.com.edtechhub.worktopicselection.service.*;
import cn.com.edtechhub.worktopicselection.model.entity.Project;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.Dept;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.com.edtechhub.worktopicselection.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 */
@Transactional
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private DeptService deptService;

    @Resource
    private TopicService topicService;

    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    @Resource
    private ProjectService projectService;

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册(实际上本项目不存在注册新用户, 只能管理员)
     */
    @PostMapping("/register")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword userUpdatePassword) {
        if (userUpdatePassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userUpdatePassword.getUserAccount();
        String userPassword = userUpdatePassword.getUserPassword();
        final String updatePassword = userUpdatePassword.getUpdatePassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, updatePassword)) {
            return null;
        }
        long result = userService.userUpdatePassword(userAccount, userPassword, updatePassword);
        return ResultUtils.success(result, "修改成功");
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO, "登录成功");
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result, "注销成功");
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user), "获取成功");
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {

        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        final String userAccount = userAddRequest.getUserAccount();
        final User oldUser = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        if (oldUser != null) {
            return ResultUtils.error(400, "该用户已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setDept(userAddRequest.getDeptName());
        user.setProject(userAddRequest.getProject());
        user.setUserRole(userAddRequest.getUserRole());

        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId(), "添加成功");
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final String userAccount = deleteRequest.getUserAccount();
        boolean b = userService.remove(new QueryWrapper<User>().eq("userAccount", userAccount));
        if (!b) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(b, "删除成功");
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true, "修改成功");
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user, "获取成功");
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user), "获取成功");
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest, request));
        return ResultUtils.success(userPage, "获取成功");
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest, request));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage, "获取成功");
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true, "修改成功");
    }

    /**
     * 获取用户数据
     *
     * @param getUserListRequest
     * @param request
     * @return
     */
    @PostMapping("/getUserList")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest getUserListRequest,
                                                      HttpServletRequest request) {
        if (getUserListRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
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
        return ResultUtils.success(userNameVO, "获取成功");
    }

    /**
     * 获取系部数据
     *
     * @param request
     * @return
     */
    @PostMapping("getDeptList")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest deptQueryRequest,
                                                  HttpServletRequest request) {
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
        return ResultUtils.success(deptVOList, "获取成功");
    }

    /**
     * 获取专业数据
     *
     * @param request
     * @return
     */
    @PostMapping("getProject")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest projectQueryRequest,
                                                  HttpServletRequest request) {
        long current = projectQueryRequest.getCurrent();
        long size = projectQueryRequest.getPageSize();
        Page<Project> projectPage = projectService.page(new Page<>(current, size),
                projectService.getQueryWrapper(projectQueryRequest, request));
        return ResultUtils.success(projectPage, "获取成功");
    }

    /**
     * 获取专业数据
     *
     * @param request
     * @return
     */
    @PostMapping("getProjectList")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest projectQueryRequest,
                                                        HttpServletRequest request) {
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
        return ResultUtils.success(projectVOList, "获取成功");
    }

    /**
     * 获取系部数据
     *
     * @param request
     * @return
     */
    @PostMapping("getDept")
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest deptQueryRequest,
                                            HttpServletRequest request) {
        long current = deptQueryRequest.getCurrent();
        long size = deptQueryRequest.getPageSize();
        Page<Dept> deptPage = deptService.page(new Page<>(current, size),
                deptService.getDeptQueryWrapper(deptQueryRequest, request));
        return ResultUtils.success(deptPage, "获取成功");
    }

    /**
     * 获取系部教师数据
     *
     * @param deptTeacherQueryRequest 查询请求参数
     * @param request                 HTTP请求
     * @return 包含教师数据的分页响应
     */
    @PostMapping("getDeptTeacher")
    public BaseResponse<Page<DeptTeacherVO>> getTeacher(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest, HttpServletRequest request) {
        if (deptTeacherQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long current = deptTeacherQueryRequest.getCurrent();
        long size = deptTeacherQueryRequest.getPageSize();
        String sortField = deptTeacherQueryRequest.getSortField();
        String sortOrder = deptTeacherQueryRequest.getSortOrder();
        final User loginUser = userService.getLoginUser(request);
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
        if (studentTopicSelection != null) {
            return ResultUtils.error(400, "您已经选择过课题");
        }
        final String dept = loginUser.getDept();

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("dept", dept);
        userQueryWrapper.eq("userRole", 1);
        userQueryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        List<User> users = userMapper.selectList(userQueryWrapper); // 假设你有 userMapper

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

        return ResultUtils.success(teacherPage, "获取成功");
    }

    /**
     * 删除用户
     *
     * @param deleteDeptRequest
     * @param request
     * @return
     */
    @PostMapping("/deleteDept")
    public BaseResponse<Boolean> deleteDept(@RequestBody DeleteDeptRequest deleteDeptRequest, HttpServletRequest request) {
        if (deleteDeptRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final String deptName = deleteDeptRequest.getDeptName();
        boolean b = deptService.remove(new QueryWrapper<Dept>().eq("deptName", deptName));
        if (!b) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(b, "删除成功");
    }

    /**
     * 删除专业
     *
     * @param deleteProjectRequest
     * @param request
     * @return
     */
    @PostMapping("/deleteProject")
    public BaseResponse<Boolean> deleteProject(@RequestBody DeleteProjectRequest deleteProjectRequest, HttpServletRequest request) {
        if (deleteProjectRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final String projectName = deleteProjectRequest.getProjectName();
        boolean b = projectService.remove(new QueryWrapper<Project>().eq("projectName", projectName));
        if (!b) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(b, "删除成功");
    }

    /**
     * 添加系部
     *
     * @param deptAddRequest
     * @param request
     * @return
     */
    @PostMapping("/addDept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest deptAddRequest, HttpServletRequest request) {
        if (deptAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        final String deptName = deptAddRequest.getDeptName();
        final Dept dept = deptService.getOne(new QueryWrapper<Dept>().eq("deptName", deptName));
        if (dept != null) {
            return ResultUtils.error(400, "该系部已存在");
        }
        Dept newDept = new Dept();
        newDept.setDeptName(deptName);
        boolean result = deptService.save(newDept);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(newDept.getId(), "添加成功");
    }

    /**
     * 添加专业
     *
     * @param projectAddRequest
     * @param request
     * @return
     */
    @PostMapping("/addProject")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest projectAddRequest, HttpServletRequest request) {
        if (projectAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        final String projectName = projectAddRequest.getProjectName();
        final String deptName = projectAddRequest.getDeptName();
        final Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", projectName));
        if (project != null) {
            return ResultUtils.error(400, "该专业已存在");
        }

        Project newProject = new Project();
        newProject.setProjectName(projectName);
        newProject.setDeptName(deptName);

        boolean result = projectService.save(newProject);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(newProject.getId(), "添加成功");
    }

    /**
     * 添加选题
     *
     * @param addTopicRequest
     * @param request
     * @return
     */
    @PostMapping("/addTopic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest addTopicRequest, HttpServletRequest request) {
        if (addTopicRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        final Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", addTopicRequest.getTopic()));
        if (oldTopic != null) {
            return ResultUtils.error(400, "该课题已存在");
        }
        final Topic topic = new Topic();
        BeanUtils.copyProperties(addTopicRequest, topic);
        if (userRole == 1) {
            topic.setTeacherName(loginUser.getUserName());
        }
        topic.setSurplusQuantity(addTopicRequest.getAmount());
        boolean result = topicService.save(topic);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(topic.getId(), "添加成功");
    }

    /**
     * 教师双选
     *
     * @param selectStudentRequest 选课请求参数
     * @param request              HTTP请求对象
     * @return 基础响应对象，包含操作结果信息
     */
    @PostMapping("/selectStudent")
    public BaseResponse<String> selectStudent(@RequestBody SelectStudentRequest selectStudentRequest, HttpServletRequest request) {
        // 检查请求参数是否为空
        if (selectStudentRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选课请求参数不能为空");
        }

        // 获取当前登录用户并检查用户角色
        User loginUser = userService.getLoginUser(request);


        // 获取选课用户账号和课题名称
        String userAccount = selectStudentRequest.getUserAccount();
        String topicName = selectStudentRequest.getTopic();

        // 根据课题名称查找课题
        Topic topic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topicName));
        if (topic == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应的课题");
        }

        Long topicId = topic.getId();

        // 同步代码块，确保课题选择的线程安全
        synchronized (topicName.intern()) {
            // 检查学生是否已经选择过课题
            StudentTopicSelection existingSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
            if (existingSelection != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "学生已经选择过课题");
            }
            //判断是否有余量
            final Integer surplusQuantity = topic.getSurplusQuantity();
            if (surplusQuantity <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "余量不足");
            }
            // 创建新的学生选题对象并保存
            StudentTopicSelection newSelection = new StudentTopicSelection();
            newSelection.setUserAccount(userAccount);
            newSelection.setTopicId(topicId);
            newSelection.setStatus(1);

            boolean saveSuccess = studentTopicSelectionService.save(newSelection);
            if (!saveSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存学生选题信息失败");
            }

            // 更新课题的剩余数量并保存
            topic.setSurplusQuantity(0);
            boolean topicSaveSuccess = topicService.saveOrUpdate(topic);
            if (!topicSaveSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新课题剩余数量失败");
            }

            // 返回成功信息
            return ResultUtils.success("1", "提交成功");
        }
    }

    /**
     * 删除选题
     *
     * @param deleteTopicRequest
     * @param request
     * @return
     */
    @PostMapping("/deleteTopic")
    public BaseResponse<Boolean> deleteTopic(@RequestBody DeleteTopicRequest deleteTopicRequest, HttpServletRequest request) {
        if (deleteTopicRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        final Long id = deleteTopicRequest.getId();
        boolean b = topicService.removeById(id);
        final boolean b1 = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", id));
        if (!b || !b1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(b, "删除成功");
    }

    /**
     * 退选
     *
     * @param deleteTopicRequest 退选请求参数
     * @param request            HTTP请求对象
     * @return 基础响应对象，包含操作结果信息
     */
    @PostMapping("/Withdraw")
    public BaseResponse<Boolean> Withdraw(@RequestBody DeleteTopicRequest deleteTopicRequest, HttpServletRequest request) {
        // 检查请求参数是否为空
        if (deleteTopicRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退选请求参数不能为空");
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
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应的课题");
            }

            // 更新课题的剩余数量
            topic.setSurplusQuantity(1); // 假设每次退选增加一个剩余数量
            boolean updateSuccess = topicService.updateById(topic);
            if (!updateSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新课题信息失败");
            }

            // 删除学生的选题记录
            boolean removeSuccess = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", topicId));
            if (!removeSuccess) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除学生选题记录失败");
            }

            // 返回成功信息
            return ResultUtils.success(true, "退选成功");
        }
    }


    /**
     * 更新选题
     *
     * @param updateTopicRequest
     * @param request
     * @return
     */
    @PostMapping("/updateTopic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest updateTopicRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
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
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        }

        return ResultUtils.success("更新成功", "更新成功");
    }


    /**
     * 重置密码
     *
     * @param request
     * @return
     */
    @PostMapping("resetPassword")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest,
                                              HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (resetPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求数据为空");
        }
        String userName = resetPasswordRequest.getUserName();
        String userAccount = resetPasswordRequest.getUserAccount();
        final User student = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userName", userName));
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该用户不存在");
        }
        final String SALT = "yupi";
        String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + "12345678").getBytes());
        student.setUserPassword(newEncryptPassword);
        student.setStatus(" ");
        final boolean b = userService.updateById(student);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改失败");
        }
        return ResultUtils.success(student.getUserAccount(), "修改成功");
    }

    /**
     * 获取选题数据
     *
     * @param request
     * @return
     */
    @PostMapping("getTopicList")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest topicQueryRequest,
                                                  HttpServletRequest request) {
        long current = topicQueryRequest.getCurrent();
        long size = topicQueryRequest.getPageSize();
        Page<Topic> topicPage = topicService.page(new Page<>(current, size),
                topicService.getTopicQueryWrapper(topicQueryRequest, request));
        return ResultUtils.success(topicPage, "获取成功");
    }

    /**
     * 添加数量
     *
     * @param request
     * @return
     */
    @PostMapping("addCount")
    public BaseResponse<String> addCount(@RequestBody AddCountRequest addCountRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = addCountRequest.getId();
        int count = addCountRequest.getCount();
        final Topic topic = topicService.getById(id);
        topic.setSurplusQuantity(topic.getSurplusQuantity() + count);
        final boolean b = topicService.updateById(topic);
        if (!b) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success("数量添加成功", "数量添加成功");
    }

    /**
     * 根据题目id获取学生
     *
     * @param request
     * @return
     */
    @PostMapping("getStudentByTopicId")
    public BaseResponse<List<User>> getStudentByTopicId(@RequestBody GetStudentByTopicId getStudentByTopicId, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (userRole != 1 && userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (getStudentByTopicId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
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
        return ResultUtils.success(userList, "数量添加成功");
    }

    /**
     * 根据题目id添加开启时间和结束时间
     *
     * @param request
     * @return
     */
    @PostMapping("setTimeById")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest setTimeRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        final Integer userRole = loginUser.getUserRole();
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (userRole != 2 && userRole != 3) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (setTimeRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
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
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
        }
        return ResultUtils.success("添加成功", "添加成功");
    }

    /**
     * 根据题目id进行预选题
     *
     * @param request
     * @return
     */
    @PostMapping("preSelectTopicById")
    public BaseResponse<Long> preSelectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (selectTopicByIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
        final Topic topic = topicService.getById(selectTopicByIdRequest.getId());
        if (selectTopicByIdRequest.getStatus() == 1) {
            if (selection != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            final Integer surplusQuantity = topic.getSurplusQuantity();
            if (surplusQuantity <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "选题余量不足");
            }
            final StudentTopicSelection studentTopicSelection = new StudentTopicSelection();
            studentTopicSelection.setUserAccount(loginUser.getUserAccount());
            studentTopicSelection.setTopicId(selectTopicByIdRequest.getId());
            final boolean save = studentTopicSelectionService.save(studentTopicSelection);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        } else {
            if (selection == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            final boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
            if (!remove) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
        }
        topic.setSelectAmount(topic.getSelectAmount() + selectTopicByIdRequest.getStatus());
        final boolean updateTopic = topicService.updateById(topic);
        if (!updateTopic) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(topic.getId(), "预选题成功");
    }

    /**
     * 根据题目id进行选题
     *
     * @param request
     * @return
     */
    @PostMapping("selectTopicById")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest selectTopicByIdRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (selectTopicByIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final Topic topic = topicService.getById(selectTopicByIdRequest.getId());
        final Date startTime = topic.getStartTime();
        final Date endTime = topic.getEndTime();
        final Date now = new Date();
        if (now.before(startTime) || now.after(endTime)) {
            throw new BusinessException(ErrorCode.NOT_TIME_ERROR);
        }
        if (topic.getSurplusQuantity() <= 0) {
            throw new BusinessException(ErrorCode.NOT_SURPLUS_ERROR);
        }
        final StudentTopicSelection selection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
        if (selectTopicByIdRequest.getStatus() == -1) {
            final boolean remove = studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()).eq("topicId", selectTopicByIdRequest.getId()));
            if (!remove) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
        } else {
            selection.setStatus(selectTopicByIdRequest.getStatus());
            final boolean update = studentTopicSelectionService.updateById(selection);
            if (!update) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        }
        topic.setSurplusQuantity(topic.getSurplusQuantity() - selectTopicByIdRequest.getStatus());
        topic.setSelectAmount(topic.getSelectAmount() - selectTopicByIdRequest.getStatus());
        final boolean updateTopic = topicService.updateById(topic);
        if (!updateTopic) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(selection.getId(), "操作成功");
    }

    /**
     * 根据获取老师姓名
     *
     * @param request
     * @return
     */
    @PostMapping("getTeacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest teacherQueryRequest, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
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
        return ResultUtils.success(teacherVOList, "获取成功");
    }

    /**
     * 获取预选题by账号
     *
     * @param request
     * @return
     */
    @PostMapping("getPreTopic")
    public BaseResponse<List<Topic>> getPreTopic(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", 0));
        if (studentTopicSelection == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        final Long topicId = studentTopicSelection.getTopicId();
        final Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        topicList.add(topic);
        return ResultUtils.success(topicList, "获取成功");
    }

    /**
     * 获取选题by账号
     *
     * @param request
     * @return
     */
    @PostMapping("getSelectTopic")
    public BaseResponse<List<Topic>> getSelectTopic(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        final String userAccount = loginUser.getUserAccount();
        final StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", 1));
        if (studentTopicSelection == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        final Long topicId = studentTopicSelection.getTopicId();
        final Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        if (topic == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        topicList.add(topic);
        return ResultUtils.success(topicList, "获取成功");
    }

    /**
     * 获取选题by题目id
     *
     * @param request
     * @return
     */
    @PostMapping("getSelectTopicById")
    public BaseResponse<List<User>> getSelectTopicById(@RequestBody GetSelectTopicById getSelectTopicById, HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        final List<StudentTopicSelection> list = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("topicId", getSelectTopicById.getId()).eq("status", 1));
        if (list == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
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
        return ResultUtils.success(userList, "获取成功");
    }

    /**
     * 获取选题情况
     *
     * @param request
     * @return
     */
    @PostMapping("getSelectTopicSituation")
    public BaseResponse<SituationVO> getSelectTopicSituation(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
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

        return ResultUtils.success(situationVO, "获取成功");
    }

    /**
     * 获取没选题学生
     *
     * @param request
     * @return
     */
    @PostMapping("getUnSelectTopicStudentList")
    public BaseResponse<List<User>> getUnSelectTopicStudentList(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
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

        return ResultUtils.success(unselectedUsers, "获取成功");
    }

    /**
     * 获取管理员获取题目
     *
     * @param request
     * @return
     */
    @PostMapping("getTopicListByAdmin")
    public BaseResponse<Page<Topic>> getTopicListByAdmin(@RequestBody TopicQueryByAdminRequest topicQueryByAdminRequest,
                                                         HttpServletRequest request) {
        long current = topicQueryByAdminRequest.getCurrent();
        long size = topicQueryByAdminRequest.getPageSize();
        Page<Topic> topicPage = topicService.page(new Page<>(current, size),
                topicService.getTopicQueryByAdminWrapper(topicQueryByAdminRequest, request));
        return ResultUtils.success(topicPage, "获取成功");
    }

    /**
     * 审核题目
     *
     * @param request
     * @return
     */
    @PostMapping("CheckTopic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest checkTopicRequest,
                                            HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        if (checkTopicRequest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        }

        Long id = checkTopicRequest.getId();
        String status = checkTopicRequest.getStatus();
        final String reason = checkTopicRequest.getReason();
        final Topic topic = topicService.getById(id);
        if (topic == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应的题目");
        }

        if (status.equals("-1")) {
            final String currentStatus = topic.getStatus();
            if (!currentStatus.equals("-2")) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前题目状态不允许此操作");
            }
        }
        if (reason != null) {
            topic.setReason(reason);
        }
        topic.setStatus(status);
        final boolean updated = topicService.updateById(topic);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新题目状态失败");
        }

        return ResultUtils.success(true, "提交成功");
    }

    /**
     * 获取系部教师数据to审核
     *
     * @param deptTeacherQueryRequest 查询请求参数
     * @param request                 HTTP请求
     * @return 包含教师数据的分页响应
     */
    @PostMapping("getDeptTeacherByAdmin")
    public BaseResponse<Page<DeptTeacherVO>> getTeacherByAdmin(@RequestBody DeptTeacherQueryRequest deptTeacherQueryRequest,
                                                               HttpServletRequest request) {
        if (deptTeacherQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
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
        List<User> users = userMapper.selectList(userQueryWrapper); // 假设你有 userMapper

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
        return ResultUtils.success(teacherPage, "获取成功");
    }
}

