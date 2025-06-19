package cn.com.edtechhub.worktopicselection;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 * 如果使用 IDEA 需要在启动前配置 .env 后才能启动
 * 如果使用 jdk 运行环境需要书写对应的环境变量才能启动
 * 如果使用 Docker 容器需要书写对应的环境变量才能启动
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
@SpringBootApplication()
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class WorkTopicSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkTopicSelectionApplication.class, args);
        log.info("http://127.0.0.1:8000/doc.html");
    }

}

// TODO: 管理端审核通过后无法修改新的审核时间...
// TODO: 一个系别的教师发布选题时, 只能选择和自己同个系别的系别, 系别主任也只能选这个主任
// TODO: 用户提交表格后好像有些问题...因为多了空格会导致前端显示不出来...
// TODO: 前端显示的登陆角色应该换一种形式展示, 不然下面太长的时候会显示奇怪
// TODO: 前端一些按钮需要优化一下, 向管理端看齐
// TODO: 思考...还是把权限加上去把...有错误再来修改
// TODO: 这里还有点问题, 无法让管理员自己重置密码, 不知道怎么回事...不知道其他用户会不会有这个问题
// TODO: 不同的系别应该看到不同的专业
// TODO: 部分的账号和密码获取没有校验长度和限制
// TODO: 前端的审核题目页面好像没有做分页, 其他的分页都需要检查一下
// TODO: 用户查看选题的时候, 回来需要做页面跳转, 但是会导致分页回到第一页, 影响用户体验...
// TODO: 前端在管理员开放某些选题时应该支持取消开放...这还缺失一个接口
// TODO: 添加一个接口, 管理员可以获取已经开放的选题数量情况, 横着出现在两个图表上方即可
// TODO: 添加广告栏, 进行网站引流
// TODO: 修改接口, 让用户看得到未到选题时间时的所有选题分页
// TODO: 批处理功能接口暂时没有验证
// TODO: 管理员无法取消开放的选题
// TODO: 主任无法取消审核通过的选题
// TODO: 几个实例的获取查询条件接口需要检查一下...
// TODO: 排序按照创建时间来排序返回, 优化用户体验
// TODO: 我感觉挺奇葩的, 教务处要求每个用户只能预先一个题目...
// TODO: /get/vo 这个接口感觉没有使用, 但是竟然给了前端未脱敏的数据...
