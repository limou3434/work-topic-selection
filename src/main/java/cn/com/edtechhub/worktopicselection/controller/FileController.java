package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.model.dto.file.UploadFileRequest;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.StudentTopicSelectionService;
import cn.com.edtechhub.worktopicselection.service.TopicService;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class FileController {

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
     * 引入选题服务依赖
     */
    @Resource
    private TopicService topicService;

    /**
     * 引入学生关联关联服务依赖
     */
    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    // 上传批量添加账号文件
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(uploadFileRequest == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "不能上传空的文件");

        String filename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(filename == null || !filename.toLowerCase().endsWith(".csv"),
                CodeBindMessageEnums.PARAMS_ERROR, "只允许上传 CSV 文件");

        return transactionTemplate.execute(transactionStatus -> {
            try (
                    Reader reader = new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8);
                    CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
            ) {
                for (CSVRecord record : csvParser) {
                    String userAccount = record.get(0).trim();
                    String name = record.get(1).trim();
                    String department = record.get(2).trim();
                    String project = "";

                    // 如果是学生就会多一些
                    if (record.size() == 4) {
                        project = record.get(3).trim();
                    }

                    // 获取或创建用户
                    User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
                    if (user == null) {
                        user = new User();
                        user.setUserAccount(userAccount);
                        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_PASSWD).getBytes());
                        user.setUserPassword(encryptPassword);
                    }

                    user.setUserName(name);
                    user.setDept(department);
                    user.setUserRole(uploadFileRequest.getStatus());
                    user.setProject(project);
                    userService.saveOrUpdate(user);
                }
            } catch (Exception e) {
                transactionStatus.setRollbackOnly(); // 确保失败回滚
                ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "批量添加失败");
            }

            return TheResult.success(CodeBindMessageEnums.SUCCESS, "批量添加成功");
        });
    }

    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/uploadTopic")
    public BaseResponse<String> uploadFileTopic(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        try (InputStream inputStream = multipartFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue; // 跳过表头
                }

                String topic = null;
                String type = null;
                String description = null;
                String requirement = null;
                String teacherName = null;
                String deptName = null;
                String deptTeacher = null;

                for (Cell cell : row) {
                    int columnIndex = cell.getColumnIndex();
                    switch (columnIndex) {
                        case 0:
                            topic = getCellStringValue(cell);
                            break;
                        case 1:
                            type = getCellStringValue(cell);
                            break;
                        case 2:
                            description = getCellStringValue(cell);
                            break;
                        case 3:
                            requirement = getCellStringValue(cell);
                            break;
                        case 4:
                            teacherName = getCellStringValue(cell);
                            break;
                        case 5:
                            deptName = getCellStringValue(cell);
                            break;
                        case 6:
                            deptTeacher = getCellStringValue(cell);
                            break;
                        default:
                            break;
                    }
                }
                final Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topic));
                if (oldTopic != null) {
                    throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "批量添加失败,有重复的题目,题目为" + topic);
                }
                Topic topic1 = new Topic();
                topic1.setTopic(topic);
                topic1.setType(type);
                topic1.setDescription(description);
                topic1.setRequirement(requirement);
                topic1.setTeacherName(teacherName);
                topic1.setDeptName(deptName);
                topic1.setDeptTeacher(deptTeacher);
                topicService.saveOrUpdate(topic1);
            }

        } catch (
                IOException e) {
            e.printStackTrace();
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "批量添加失败");
        }

        return new BaseResponse<>(0, "成功", "批量添加成功");
    }

    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/unselect/topic/student/list")
    public void getUnSelectTopicStudentListCsv(HttpServletRequest request, HttpServletResponse response) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final String dept = loginUser.getDept();
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", 0).eq("dept", dept));
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>().eq("status", 1));
        Set<String> selectedUserAccounts = selectedList.stream()
                .map(StudentTopicSelection::getUserAccount)
                .collect(Collectors.toSet());
        List<User> unselectedUsers = userList.stream()
                .filter(user -> !selectedUserAccounts.contains(user.getUserAccount()))
                .collect(Collectors.toList());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=没有选题学生列表.csv");

        try (ServletOutputStream outputStream = response.getOutputStream();
             Writer writer = new OutputStreamWriter(outputStream);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("用户账号", "用户名", "专业", "系部"))) {

            for (User user : unselectedUsers) {
                csvPrinter.printRecord(user.getUserAccount(), user.getUserName(), user.getProject(), user.getDept());
            }

            csvPrinter.flush();
        } catch (
                IOException e) {
            e.printStackTrace();
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "导出CSV失败");
        }
    }

    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/student/list")
    public void getSelectTopicStudentListCsv(HttpServletRequest request, HttpServletResponse response) {
        final User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(CodeBindMessageEnums.NO_LOGIN_ERROR, "");
        }
        final String dept = loginUser.getDept();
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list();
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

        // 设置HTTP响应的内容类型和文件名
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=已选题学生列表.csv");

        try (ServletOutputStream outputStream = response.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("学号", "姓名", "专业", "系部", "题目", "指导老师"))) {

            // 将已选题学生信息写入CSV文件
            for (User user : userList) {
                csvPrinter.printRecord(user.getUserAccount(), user.getUserName(), user.getProject(), user.getDept(),
                        topicList.get(userList.indexOf(user)).getTopic(), topicList.get(userList.indexOf(user)).getTeacherName());
            }

            csvPrinter.flush(); // 刷新CSV打印器
        } catch (
                IOException e) {
            e.printStackTrace();
            throw new BusinessException(CodeBindMessageEnums.OPERATION_ERROR, "导出CSV失败");
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

}
