package cn.com.edtechhub.worktopicselection.model.vo;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 系统信息视图
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class TheSystemInfoVO {

    /**
     * 当前系统使用内存资源
     */
    private String memoryUsage;

    /**
     * 当前系统使用 CPU 资源
     */
    private String cpuUsage;

    /**
     * 当前系统使用硬盘资源
     */
    private String diskUsage;

    /**
     * 当前 JVM 虚拟内存资源
     */
    private String jvmMemoryUsage;

//    /**
//     * 当前帐号总数
//     */
//    private Integer totalUserCount;
//
//    /**
//     * 当前主任数量
//     */
//    private Integer totalDeptCount;
//
//    /**
//     * 当前教师数量
//     */
//    private Integer totalTeacherCount;
//
//    /**
//     * 当前学生数量
//     */
//    private Integer totalStudentCount;
//
//    /**
//     * 当前系统有哪些用户还没有登陆过
//     */
//    private List<User> noLoginUserList;
//
//    /**
//     * 当前系统有哪些教师没有出完题目
//     */
//    private List<User> noOutTopicTeacherList;
//
//    /**
//     * 当前系统审核打回的题目数量
//     */
//    private Integer auditBackTopicCount;
//
//    /**
//     * 当前系统审核通过的题目数量
//     */
//    private Integer auditPassTopicCount;
//
//    /**
//     * 当前系统处于发布的题目数量
//     */
//    private Integer releaseTopicCount;

}
