package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.mapper.TopicMapper;
import cn.com.edtechhub.worktopicselection.model.dto.topic.TopicQueryByAdminRequest;
import cn.com.edtechhub.worktopicselection.model.dto.topic.TopicQueryRequest;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.service.StudentTopicSelectionService;
import cn.com.edtechhub.worktopicselection.service.TopicService;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【topic】的数据库操作Service实现
 * @createDate 2024-05-22 14:41:18
 */
@Service
@Transactional
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    private UserService userService;

    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    @Override
    public QueryWrapper<Topic> getQueryWrapper(TopicQueryRequest topicQueryRequest) {
        ThrowUtils.throwIf(topicQueryRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");

        User loginUser = userService.userGetCurrentLoginUser();
        Integer userRole = loginUser.getUserRole();
        Integer status = topicQueryRequest.getStatus();
        String sortField = topicQueryRequest.getSortField();
        String sortOrder = topicQueryRequest.getSortOrder();
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(status != null, "status", status);
        if (loginUser.getUserRole() == 2) {
            queryWrapper.eq(StringUtils.isNotBlank(loginUser.getDept()), "deptName", loginUser.getDept());
        }
        if (loginUser.getUserRole() == 1) {
            queryWrapper.eq("teacherName", loginUser.getUserName());
        }
        if (loginUser.getUserRole() == 0) {
            // 如果是学生检查是否有关联过题目, 如果有就不允许显示这些被关联的题目
            List<StudentTopicSelection> selections = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("userAccount", loginUser.getUserAccount()));
            if (!selections.isEmpty()) {
                List<Long> topicIds = selections.stream()
                        .map(StudentTopicSelection::getTopicId)
                        .collect(Collectors.toList());
                queryWrapper.notIn("id", topicIds);
            }
        }
        queryWrapper.like(StringUtils.isNotBlank(topicQueryRequest.getTopic()), "topic", topicQueryRequest.getTopic());
        queryWrapper.like(StringUtils.isNotBlank(topicQueryRequest.getType()), "type", topicQueryRequest.getType());
        queryWrapper.eq(StringUtils.isNotBlank(topicQueryRequest.getTeacherName()), "teacherName", topicQueryRequest.getTeacherName());
        queryWrapper.eq(StringUtils.isNotBlank(topicQueryRequest.getDeptName()), "deptName", topicQueryRequest.getDeptName());
        queryWrapper.eq(StringUtils.isNotBlank((CharSequence) topicQueryRequest.getStartTime()), "startTime", topicQueryRequest.getStartTime());
        queryWrapper.eq(StringUtils.isNotBlank((CharSequence) topicQueryRequest.getEndTime()), "endTime", topicQueryRequest.getEndTime());
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public QueryWrapper<Topic> getTopicQueryByAdminWrapper(TopicQueryByAdminRequest topicQueryByAdminRequest) {
        // 检查请求参数是否为空
        if (topicQueryByAdminRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");
        }

        // 检查用户是否登录
        final User loginUser = userService.userGetCurrentLoginUser();

        // 获取排序字段和排序顺序
        String sortField = topicQueryByAdminRequest.getSortField();
        String sortOrder = topicQueryByAdminRequest.getSortOrder();

        // 创建查询包装器
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(topicQueryByAdminRequest.getTopic()), "topic", topicQueryByAdminRequest.getTopic());
        queryWrapper.like(StringUtils.isNotBlank(topicQueryByAdminRequest.getType()), "type", topicQueryByAdminRequest.getType());
        queryWrapper.eq(StringUtils.isNotBlank(topicQueryByAdminRequest.getTeacherName()), "teacherName", topicQueryByAdminRequest.getTeacherName());
        queryWrapper.eq(StringUtils.isNotBlank(topicQueryByAdminRequest.getDeptName()), "deptName", topicQueryByAdminRequest.getDeptName());
        queryWrapper.eq(StringUtils.isNotBlank((CharSequence) topicQueryByAdminRequest.getStartTime()), "startTime", topicQueryByAdminRequest.getStartTime());
        queryWrapper.eq(StringUtils.isNotBlank((CharSequence) topicQueryByAdminRequest.getEndTime()), "endTime", topicQueryByAdminRequest.getEndTime());
        queryWrapper.eq(StringUtils.isNotBlank(loginUser.getDept()), "deptName", loginUser.getDept());
        // 设置查询条件，筛选出剩余问题数量为1的题目·
        queryWrapper.eq("surplusQuantity", 1);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}




