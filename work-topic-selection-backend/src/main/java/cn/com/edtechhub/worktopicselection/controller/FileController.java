package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.sentine.SentineManager;
import cn.com.edtechhub.worktopicselection.model.dto.file.UploadFileRequest;
import cn.com.edtechhub.worktopicselection.model.entity.*;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.*;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 文件控制层
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@RestController
@RequestMapping("/file")
public class FileController {

    /**
     * 注入 SentineManager 依赖
     */
    @Resource
    SentineManager sentineManager;

    /**
     * 注入 SQL 导出服务依赖
     */
    @Resource
    private SqlExportService sqlExportService;

    /**
     * 注入事务管理依赖
     */
    @Resource
    TransactionTemplate transactionTemplate;

    /**
     * 注入用户服务依赖
     */
    @Resource
    private UserService userService;

    /**
     * 注入选题服务依赖
     */
    @Resource
    private TopicService topicService;

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
     * 注入学生关联关联服务依赖
     */
    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    /**
     * 根据模板文件批量添加角色账号
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, UploadFileRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "不能上传空的文件");
        assert multipartFile != null;

        String filename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(filename == null || !filename.toLowerCase().endsWith(".csv"), CodeBindMessageEnums.PARAMS_ERROR, "只允许上传 CSV 文件");

        AtomicLong i = new AtomicLong();

        // 批量添加用户账号
        return transactionTemplate.execute(transactionStatus -> {
            try (
                    Reader reader = new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8);
                    CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
            ) {
                for (CSVRecord record : csvParser) {
                    i.getAndIncrement();

                    // 获取表格数据
                    String userAccount = record.get(0).trim();
                    String name = record.get(1).trim();
                    String department = record.get(2).trim();
                    String project = record.get(3).trim();
                    String topicAmount = "";
                    if (record.size() == 5) {
                        // 说明导入名单使用教师帐号
                        userAccount = "JSZH20250000" + i;
                        topicAmount = record.get(4).trim();
                        ThrowUtils.throwIf(StringUtils.isBlank(topicAmount), CodeBindMessageEnums.PARAMS_ERROR, "表中第 " + i + " 行数据没有填写该教师的最大出题数量, 请检查后重新上传: " + record);
                        ThrowUtils.throwIf(StringUtils.isNotBlank(topicAmount) && !StringUtils.isNumeric(topicAmount), CodeBindMessageEnums.PARAMS_ERROR, "表中第 " + i + " 行数据填写的该教师最大出题数量有误, 必须是正整数, 请检查后重新上传: " + record);
                    }

                    // 检查表格空白填写的问题
                    ThrowUtils.throwIf(StringUtils.isAllBlank(userAccount, name, department, project), CodeBindMessageEnums.PARAMS_ERROR, "表中第 " + i + " 行数据有空白天蝎, 请检查后重新上传: " + record);

                    // 检查用户账号是否已存在, 存在则跳过
                    User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
                    if (user != null) {
                        continue;
                    }

                    // 创建新的用户
                    user = new User();
                    user.setUserAccount(userAccount);
                    user.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes()));
                    user.setUserName(name);
                    user.setUserRole(request.getStatus());
                    user.setDept(department);
                    user.setProject(project);
                    user.setTopicAmount(StringUtils.isBlank(topicAmount) ? null : Integer.parseInt(topicAmount));

                    // 保存之前先检查用户填写的系部和专业是否存在, 不存在直接抛出异常回滚
                    if (StringUtils.isNotBlank(department)) {
                        ThrowUtils.throwIf(deptService.getOne(new QueryWrapper<Dept>().eq("deptName", department)) == null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "表中第 " + i + "行, 导入用户 " + "[" + userAccount + ", " + name + "] " + "时, 系部 [" + department + "] 在系统中不存在, 请添加该系部或修改表格");
                    }
                    if (StringUtils.isNotBlank(project)) {
                        ThrowUtils.throwIf(projectService.getOne(new QueryWrapper<Project>().eq("projectName", project)) == null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "表中第 " + i + "行, 导入用户 " + "[" + userAccount + ", " + name + "]" + "时, 专业 [" + project + "] 在系统中不存在, 请添加该专业或修改表格");
                    }

                    // 保存用户
                    userService.saveOrUpdate(user);
                }
            } catch (BusinessException e) {
                ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, e.getExceptionMessage());
            } catch (Exception e) {
                transactionStatus.setRollbackOnly(); // 确保失败回滚
                ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "批量添加失败" + e.getMessage());
            }

            return TheResult.success(CodeBindMessageEnums.SUCCESS, "批量添加成功");
        });
    }

    /**
     * 根据模板文件批量添加毕设选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/upload/topic")
    public BaseResponse<String> uploadFileTopic(@RequestPart("file") MultipartFile multipartFile) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        return TheResult.notyet("该功能有缺陷暂时不开放使用！");

//        // 检查参数
//        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "不能上传空的文件");
//        assert multipartFile != null;
//
//        String filename = multipartFile.getOriginalFilename();
//        ThrowUtils.throwIf(filename == null || !filename.toLowerCase().endsWith(".csv"), CodeBindMessageEnums.PARAMS_ERROR, "只允许上传 CSV 文件");
//
//        // 批量添加教师题目
//        return transactionTemplate.execute(transactionStatus -> {
//            try (
//                    Reader reader = new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8);
//                    CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
//            ) {
//                for (CSVRecord record : csvParser) {
//                    String topicName = record.get(0).trim();
//                    String topicType = record.get(1).trim();
//                    String topicDescription = record.get(2).trim();
//                    String topicRequirement = record.get(3).trim();
//                    String topicDeptName = record.get(4).trim();
//                    String topicDeptTeacher = record.get(5).trim();
//
//                    // 创建题目
//                    Topic topic = new Topic();
//
//                    topic.setTopic(topicName);
//                    topic.setType(topicType);
//                    topic.setDescription(topicDescription);
//                    topic.setRequirement(topicRequirement);
//                    topic.setDeptName(topicDeptName);
//                    topic.setDeptTeacher(topicDeptTeacher);
//                    topic.setTeacherName(userService.userGetCurrentLoginUser().getUserName());
//                    topic.setSurplusQuantity(1);
//                    topic.setSelectAmount(0);
//                    topic.setStatus(TopicStatusEnum.PENDING_REVIEW.getCode());
//
//                    topicService.save(topic);
//                }
//            } catch (IOException e) {
//                transactionStatus.setRollbackOnly(); // 确保失败回滚
//                ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "批量添加失败");
//            }
//
//            return new BaseResponse<>(0, "成功", "批量添加成功");
//        });
    }

    /**
     * 获取已经选择的学生题目列表
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/student/list")
    public void getSelectTopicStudentListCsv(HttpServletResponse httpServletResponse) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        User loginUser = userService.userGetCurrentLoginUser();
        String dept = loginUser.getDept();

        List<StudentTopicSelection> selectedList = studentTopicSelectionService.list();
        List<User> userList = new ArrayList<>();
        List<Topic> topicList = new ArrayList<>();

        // 获取已选题学生的用户信息和选题信息
        for (StudentTopicSelection studentTopicSelection : selectedList) {
            String userAccount = studentTopicSelection.getUserAccount();
            User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq(StringUtils.isNotBlank(dept), "dept", dept));
            if (user != null) {
                userList.add(user);
                Topic topic = topicService.getById(studentTopicSelection.getTopicId());
                if (topic != null) {
                    topicList.add(topic);
                }
            }
        }

        // 设置 HTTP 响应的内容类型和文件名
        httpServletResponse.setContentType("text/csv");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=已选题学生列表.csv");

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("学号", "姓名", "专业", "系部", "题目", "指导老师"))) {

            // 将已选题学生信息写入 CSV 文件
            for (User user : userList) {
                csvPrinter.printRecord(user.getUserAccount(), user.getUserName(), user.getProject(), user.getDept(),
                        topicList.get(userList.indexOf(user)).getTopic(), topicList.get(userList.indexOf(user)).getTeacherName());
            }

            csvPrinter.flush(); // 刷新 CSV 打印器
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

    /**
     * 获取尚未选择的学生题目列表
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/get/unselect/topic/student/list")
    public void getUnSelectTopicStudentListCsv(HttpServletResponse httpServletResponse) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        SphU.entry(entryName);

        User loginUser = userService.userGetCurrentLoginUser();
        String dept = loginUser.getDept();

        List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", 0).eq("dept", dept));
        List<StudentTopicSelection> selectedList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("status", 1));
        Set<String> selectedUserAccounts = selectedList.stream()
                .map(StudentTopicSelection::getUserAccount)
                .collect(Collectors.toSet());
        List<User> unselectedUsers = userList.stream()
                .filter(user -> !selectedUserAccounts.contains(user.getUserAccount()))
                .collect(Collectors.toList());

        httpServletResponse.setContentType("text/csv");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=没有选题学生列表.csv");

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             Writer writer = new OutputStreamWriter(outputStream);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("用户账号", "用户名", "专业", "系部"))) {

            for (User user : unselectedUsers) {
                csvPrinter.printRecord(user.getUserAccount(), user.getUserName(), user.getProject(), user.getDept());
            }

            csvPrinter.flush();
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

    /**
     * 导出系统内所有帐号
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/export/user_list")
    public void exportUserList(HttpServletResponse httpServletResponse) {
        String fileName = "系统内所有帐号.csv";
        String sql = "SELECT\n" +
                "    `userAccount` AS 帐号,\n" +
                "    `userName` AS 姓名,\n" +
                "    CASE `userRole`\n" +
                "        WHEN 3 THEN '管理员'\n" +
                "        WHEN 2 THEN '主任'\n" +
                "        WHEN 1 THEN '教师'\n" +
                "        WHEN 0 THEN '学生'\n" +
                "        END AS 角色,\n" +
                "    `dept` AS 系部,\n" +
                "    `project` AS 专业,\n" +
                "    `email` AS 邮箱,\n" +
                "    `topicAmount` AS 出题数量或预选数量,\n" +
                "    `status` AS 状态\n" +
                "    FROM `user` WHERE `isDelete` = 0;";

        // 调用 SQL 服务获取结果
        List<Map<String, Object>> rows = sqlExportService.executeQuery(sql);

        httpServletResponse.setContentType("text/csv");
        String encodedFileName = null;
        try {
            encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName);

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader(rows.isEmpty() ? new String[]{"无数据"} : rows.get(0).keySet().toArray(new String[0])))) {

            // 遍历行写入 CSV
            for (Map<String, Object> row : rows) {
                List<Object> values = new ArrayList<>();
                for (String key : row.keySet()) {
                    values.add(row.get(key));
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

    /**
     * 导出系统内所有题目
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/export/topic_list")
    public void exportTopicList(HttpServletResponse httpServletResponse) {
        String fileName = "所有未出题教师.csv";
        String sql = "SELECT\n" +
                "    `teacherName` AS 教师名称,\n" +
                "    `topic` AS 题目,\n" +
                "    `type` AS 题目类型,\n" +
                "    `description` AS 描述,\n" +
                "    `requirement` AS 要求,\n" +
                "    `deptName` AS 系部,\n" +
                "    `deptTeacher` AS 系部主任,\n" +
                "    CASE `status`\n" +
                "        WHEN -2 THEN '被打回'\n" +
                "        WHEN -1 THEN '待审核'\n" +
                "        WHEN 0 THEN '没发布'\n" +
                "        WHEN 1 THEN '已发布'\n" +
                "        END AS 状态,\n" +
                "    `reason` AS 打回理由\n" +
                "    FROM `topic` WHERE `isDelete` = 0;";

        // 调用 SQL 服务获取结果
        List<Map<String, Object>> rows = sqlExportService.executeQuery(sql);

        httpServletResponse.setContentType("text/csv");
        String encodedFileName = null;
        try {
            encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName);

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader(rows.isEmpty() ? new String[]{"无数据"} : rows.get(0).keySet().toArray(new String[0])))) {

            // 遍历行写入 CSV
            for (Map<String, Object> row : rows) {
                List<Object> values = new ArrayList<>();
                for (String key : row.keySet()) {
                    values.add(row.get(key));
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

    /**
     * 导出系统内已选学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/export/student_topic_list/en_select")
    public void exportStudentTopicListEnSelect(HttpServletResponse httpServletResponse) {
        String fileName = "已选学生.csv";
        String sql = "SELECT\n" +
                "    u.`userAccount` AS `学号`,\n" +
                "    u.`userName` AS `姓名`,\n" +
                "    u.`dept` AS `系部`,\n" +
                "    u.`project` AS `专业`,\n" +
                "    u.`email` AS `邮箱`,\n" +
                "    s.`createTime` AS `确认时间`,\n" +
                "    t.`teacherName` AS `指导教师`,\n" +
                "    t.`topic`       AS `题目标题`,\n" +
                "    t.`description` AS `题目内容`,\n" +
                "    t.`requirement` AS `题目要求`\n" +
                "FROM `student_topic_selection` s\n" +
                "JOIN `user` u ON s.userAccount = u.userAccount\n" +
                "JOIN `topic` t ON s.topicId = t.id\n" +
                "WHERE\n" +
                "    s.status = 2 AND\n" +
                "    u.userRole = 0 AND\n" +
                "    s.isDelete = 0 AND\n" +
                "    u.isDelete = 0 AND\n" +
                "    t.isDelete = 0;\n";

        // 调用 SQL 服务获取结果
        List<Map<String, Object>> rows = sqlExportService.executeQuery(sql);

        httpServletResponse.setContentType("text/csv");
        String encodedFileName = null;
        try {
            encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName);

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader(rows.isEmpty() ? new String[]{"无数据"} : rows.get(0).keySet().toArray(new String[0])))) {

            // 遍历行写入 CSV
            for (Map<String, Object> row : rows) {
                List<Object> values = new ArrayList<>();
                for (String key : row.keySet()) {
                    values.add(row.get(key));
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

    /**
     * 导出系统内未选学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/export/student_topic_list/un_select")
    public void exportStudentTopicListUnSelect(HttpServletResponse httpServletResponse) {
        String fileName = "未选学生.csv";
        String sql = "SELECT\n" +
                "    u.`userAccount` AS `学号`,\n" +
                "    u.`userName`    AS `姓名`,\n" +
                "    u.`dept`        AS `系部`,\n" +
                "    u.`project`     AS `专业`,\n" +
                "    u.`email`       AS `邮箱`\n" +
                "FROM `user` u\n" +
                "WHERE\n" +
                "    u.userRole = 0 AND\n" +
                "    u.isDelete = 0 AND\n" +
                "NOT EXISTS (\n" +
                "    SELECT 1\n" +
                "    FROM student_topic_selection s\n" +
                "    WHERE\n" +
                "        s.userAccount = u.userAccount AND\n" +
                "        s.status = 2 AND\n" +
                "        s.isDelete = 0\n" +
                ");\n";

        // 调用 SQL 服务获取结果
        List<Map<String, Object>> rows = sqlExportService.executeQuery(sql);

        httpServletResponse.setContentType("text/csv");
        String encodedFileName = null;
        try {
            encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName);

        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.withHeader(rows.isEmpty() ? new String[]{"无数据"} : rows.get(0).keySet().toArray(new String[0])))) {

            // 遍历行写入 CSV
            for (Map<String, Object> row : rows) {
                List<Object> values = new ArrayList<>();
                for (String key : row.keySet()) {
                    values.add(row.get(key));
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        } catch (IOException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "导出 CSV 失败");
        }
    }

}
