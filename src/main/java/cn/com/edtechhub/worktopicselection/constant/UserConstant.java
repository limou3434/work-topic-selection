package cn.com.edtechhub.worktopicselection.constant;

/**
 * 用户常量
 *
 *  
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 学生
     */
    int DEFAULT_ROLE = 0;

    /**
     * 教师
     */
    int TEACHER = 1;
    /**
     * 系部
     */
    int DEPT = 2;
    /**
     * 管理员
     */
    int ADMIN = 3;


    // endregion
}
