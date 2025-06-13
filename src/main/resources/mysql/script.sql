-- 创建库
create database if not exists bsxt_system;

-- 切换库
use bsxt_system;

create table dept
(
    id         bigint auto_increment comment 'id'
        primary key,
    deptName   varchar(256)                       not null comment '系部名',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '系部' collate = utf8mb4_unicode_ci;

create index deptName
    on dept (deptName);

create table student_topic_selection
(
    id          bigint auto_increment comment 'id'
        primary key,
    userAccount varchar(256)                       not null comment '账号',
    topicId     bigint                             not null comment '题目id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    status      int      default 0                 not null comment '选题状态：0--预选，1--抢到'
)
    comment '学生选题' collate = utf8mb4_unicode_ci;

create index userAccount
    on student_topic_selection (userAccount);

create table topic
(
    id              bigint auto_increment comment 'id'
        primary key,
    topic           varchar(255)                           null comment '题目',
    type            varchar(255)                           null comment '题目类型',
    description     longtext                               null comment '题目描述',
    requirement     longtext                               null comment '对学生要求',
    teacherName     varchar(256)                           null comment '指导老师',
    deptName        varchar(256)                           null comment '系部名',
    deptTeacher     varchar(256)                           null comment '系部主任',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0                 not null comment '是否删除',
    surplusQuantity int                                    null comment '剩余数量',
    amount          int                                    null comment '总量',
    startTime       datetime                               null comment '开启时间',
    endTime         datetime                               null,
    status          varchar(256) default '0'               not null comment '是否发布,1-以发布，0-没发布',
    selectAmount    int          default 0                 null comment '预选人数'
)
    charset = utf8mb3;

create table user
(
    userName     varchar(256)                       null comment '用户姓名',
    id           bigint auto_increment comment 'id',
    userAccount  varchar(256)                       not null comment '账号',
    userPassword varchar(512)                       not null comment '密码',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 教师 2 - 系部 3 - 管理员',
    dept         varchar(256)                       null comment '系部',
    status       varchar(256)                       null,
    primary key (id, userAccount)
)
    comment '用户';


