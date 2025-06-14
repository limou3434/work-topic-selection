-- 项目数库
DROP DATABASE IF EXISTS `work_topic_selection`;
CREATE DATABASE `work_topic_selection` CHARACTER SET `utf8mb4` COLLATE = `utf8mb4_unicode_ci`;
USE `work_topic_selection`;

-- 项目用户
DROP USER IF EXISTS 'wts'@'%';
CREATE USER 'wts'@'%' IDENTIFIED BY 'Qwe54188_';
GRANT ALL PRIVILEGES ON `work_topic_selection`.* TO 'wts'@'%';
FLUSH PRIVILEGES;

-- 项目数表
CREATE TABLE `user`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  VARCHAR(256) NOT NULL COMMENT '账号',
    `userName`     VARCHAR(256)          DEFAULT NULL COMMENT '用户姓名',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
    `createTime`   DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `userRole`     INT          NOT NULL DEFAULT '0' COMMENT '用户角色 0 - 普通用户 1 - 教师 2 - 系部 3 - 管理员',
    `dept`         VARCHAR(256)          DEFAULT NULL COMMENT '系部',
    `status`       VARCHAR(256)          DEFAULT NULL COMMENT '账号状态',
    `project`      VARCHAR(256)          DEFAULT NULL COMMENT '专业',
    `topicAmount`  INT                   DEFAULT NULL COMMENT '',
    PRIMARY KEY (`id`,
                 `userAccount`) USING BTREE COMMENT '主键',
    UNIQUE KEY `uniUserAccount` (`userAccount`) COMMENT '账号唯一约束',
    INDEX `idxUserName` (`userName`) COMMENT '提升基于用户名称的查询性能'
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb4`
    COLLATE = `utf8mb4_0900_ai_ci`
    ROW_FORMAT = DYNAMIC COMMENT ='用户表'
;

CREATE TABLE `dept`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `deptName`   VARCHAR(256) NOT NULL COMMENT '系部名',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `deptName` (`deptName`) USING BTREE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb4`
    COLLATE = `utf8mb4_0900_ai_ci`
    ROW_FORMAT = DYNAMIC COMMENT ='系部表'
;

CREATE TABLE `project`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `projectName` VARCHAR(256) NOT NULL COMMENT '专业名',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `deptName`    VARCHAR(256)          DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb4`
    COLLATE = `utf8mb4_0900_ai_ci`
    ROW_FORMAT = DYNAMIC COMMENT ='专业表'
;

CREATE TABLE `topic`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `topic`           VARCHAR(255)          DEFAULT NULL COMMENT '题目',
    `type`            VARCHAR(255)          DEFAULT NULL COMMENT '题目类型',
    `description`     LONGTEXT COMMENT '题目描述',
    `requirement`     LONGTEXT COMMENT '对学生要求',
    `teacherName`     VARCHAR(256)          DEFAULT NULL COMMENT '指导老师',
    `deptName`        VARCHAR(256)          DEFAULT NULL COMMENT '系部名',
    `deptTeacher`     VARCHAR(256)          DEFAULT NULL COMMENT '系部主任',
    `createTime`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`        TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `surplusQuantity` INT          NOT NULL DEFAULT '1' COMMENT '剩余数量',
    `startTime`       DATETIME              DEFAULT NULL COMMENT '开启时间',
    `endTime`         DATETIME              DEFAULT NULL COMMENT '开始时间',
    `status`          VARCHAR(256) NOT NULL DEFAULT '-1' COMMENT '是否发布, 1-以发布，0-没发布， -1-代审核， -2-打回',
    `selectAmount`    INT                   DEFAULT '0' COMMENT '预选人数',
    `reason`          VARCHAR(256)          DEFAULT NULL COMMENT '打回理由',
    PRIMARY KEY (`id`) USING BTREE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb3`
    ROW_FORMAT = DYNAMIC COMMENT ='选题表'
;

CREATE TABLE `student_topic_selection`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
    `topicId`     BIGINT       NOT NULL COMMENT '题目id',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `status`      INT          NOT NULL DEFAULT '0' COMMENT '选题状态：0--预选，1--抢到',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `userAccount` (`userAccount`) USING BTREE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb4`
    COLLATE = `utf8mb4_0900_ai_ci`
    ROW_FORMAT = DYNAMIC COMMENT ='用户选题关联表'
;

-- 项目数据
INSERT INTO `dept`
VALUES (1,
        '电子与移动通信工程系',
        '2024-06-14 20:36:08',
        '2024-06-14 20:36:08',
        0),
       (2,
        '电气工程与自动化系',
        '2024-06-14 20:36:19',
        '2024-06-14 20:36:19',
        0),
       (3,
        '计算机科学与工程系',
        '2024-06-14 20:36:31',
        '2024-06-14 20:36:31',
        0),
       (4,
        '大数据与人工智能系',
        '2024-06-14 20:36:52',
        '2024-06-14 20:36:52',
        0)
;

INSERT INTO `project`
VALUES (1,
        '电子信息科学与技术',
        '2024-06-14 20:37:16',
        '2024-06-14 20:37:16',
        0,
        '电子与移动通信工程系'),
       (2,
        '通信工程',
        '2024-06-14 20:37:29',
        '2024-06-14 20:37:29',
        0,
        '电子与移动通信工程系'),
       (3,
        '电气工程及其自动化',
        '2024-06-14 20:37:46',
        '2024-06-14 20:37:46',
        0,
        '电气工程与自动化系'),
       (4,
        '计算机科学与技术',
        '2024-06-14 20:38:10',
        '2024-06-14 20:38:10',
        0,
        '计算机科学与工程系'),
       (5,
        '软件工程',
        '2024-06-14 20:38:16',
        '2024-06-14 20:38:16',
        0,
        '计算机科学与工程系'),
       (6,
        '数据科学与大数据技术',
        '2024-06-14 20:38:33',
        '2024-06-14 20:38:33',
        0,
        '大数据与人工智能系'),
       (7,
        '智能科学与技术',
        '2024-06-14 20:38:49',
        '2024-06-14 20:38:49',
        0,
        '大数据与人工智能系')
;

INSERT INTO `user`
VALUES (1,
        'test-admin',
        '测试管理',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        3,
        NULL,
        '老用户',
        NULL,
        NULL),
       (2,
        'test-dept-dz',
        '测试主任(电子)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        2,
        '电子与移动通信工程系',
        '老用户',
        NULL,
        NULL),
       (3,
        'test-dept-dq',
        '测试主任(电气)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        2,
        '电气工程与自动化系',
        '老用户',
        NULL,
        NULL),
       (4,
        'test-dept-jk',
        '测试主任(计科)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        2,
        '计算机科学与工程系',
        '老用户',
        NULL,
        NULL),
       (5,
        'test-dept-sz',
        '测试主任(数智)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        2,
        '大数据与人工智能系',
        '老用户',
        NULL,
        NULL),
       (6,
        'test-teacher-dz',
        '测试教师(电子)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        1,
        '电子与移动通信工程系',
        '老用户',
        NULL,
        NULL),
       (7,
        'test-teacher-dq',
        '测试教师(电气)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        1,
        '电气工程与自动化系',
        '老用户',
        NULL,
        NULL),
       (8,
        'test-teacher-jk',
        '测试教师(计科)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        1,
        '计算机科学与工程系',
        '老用户',
        NULL,
        NULL),
       (9,
        'test-teacher-sz',
        '测试教师(数智)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        1,
        '大数据与人工智能系',
        '老用户',
        NULL,
        NULL),
       (10,
        'test-student-dz',
        '测试学生(电子)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        0,
        '电子与移动通信工程系',
        '老用户',
        '电子信息科学与技术',
        NULL),
       (11,
        'test-student-dq',
        '测试学生(电气)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        0,
        '电气工程与自动化系',
        '老用户',
        '电气工程及其自动化',
        NULL),
       (12,
        'test-student-jk',
        '测试学生(计科)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        0,
        '计算机科学与工程系',
        '老用户',
        '计算机科学与技术',
        NULL),
       (13,
        'test-student-sz',
        '测试学生(数智)',
        'bde672b1737093d5f9ad55ccc26f32eb',
        '2025-06-15 13:25:49',
        '2025-06-15 13:25:49',
        0,
        0,
        '大数据与人工智能系',
        '老用户',
        '数据科学与大数据技术',
        NULL)
;

INSERT INTO `topic`
VALUES (1934290749612257282,
        '电子人的毕设题目1',
        '电子',
        'abababab',
        'abababab',
        '测试教师(电子)',
        '电子与移动通信工程系',
        '测试主任(电子)',
        '2025-06-16 00:43:46',
        '2025-06-16 00:43:46',
        0,
        1,
        NULL,
        NULL,
        '0',
        0,
        NULL),
       (1934291277817737218,
        '电子人的毕设题目2',
        '电子',
        'abababab',
        'abababab',
        '测试教师(电子)',
        '电子与移动通信工程系',
        '测试主任(电子)',
        '2025-06-16 00:45:52',
        '2025-06-16 00:45:52',
        0,
        1,
        NULL,
        NULL,
        '-1',
        0,
        NULL),
       (1934291468499185665,
        '电气人的毕设题目1',
        '电气',
        'abababab',
        'abababab',
        '测试教师(电气)',
        '电气工程与自动化系',
        '测试主任(电气)',
        '2025-06-16 00:46:38',
        '2025-06-16 00:46:38',
        0,
        1,
        NULL,
        NULL,
        '0',
        0,
        NULL),
       (1934291575281971201,
        '电气人的毕设题目2',
        '电气',
        'abababab',
        'abababab',
        '测试教师(电气)',
        '电气工程与自动化系',
        '测试主任(电气)',
        '2025-06-16 00:47:03',
        '2025-06-16 00:47:03',
        0,
        1,
        NULL,
        NULL,
        '-1',
        0,
        NULL),
       (1934291739598024706,
        '计科人的毕设题目1',
        '计科',
        'abababab',
        'abababab',
        '测试教师(计科)',
        '计算机科学与工程系',
        '测试主任(计科)',
        '2025-06-16 00:47:42',
        '2025-06-16 00:47:42',
        0,
        1,
        NULL,
        NULL,
        '0',
        1,
        NULL),
       (1934293798057570306,
        '计科人的毕设题目2',
        '计科',
        'abababab',
        'abababab',
        '测试教师(计科)',
        '计算机科学与工程系',
        '测试主任(计科)',
        '2025-06-16 00:55:53',
        '2025-06-16 00:55:53',
        0,
        1,
        NULL,
        NULL,
        '-1',
        0,
        NULL),
       (1934294296957448194,
        '数智人的毕设题目1',
        '数智',
        'abababab',
        'abababab',
        '测试教师(数智)',
        '大数据与人工智能系',
        '测试主任(数智)',
        '2025-06-16 00:57:52',
        '2025-06-16 00:57:52',
        0,
        1,
        NULL,
        NULL,
        '0',
        0,
        NULL),
       (1934294375428681729,
        '数智人的毕设题目2',
        '数智',
        'abababab',
        'abababab',
        '测试教师(数智)',
        '大数据与人工智能系',
        '测试主任(数智)',
        '2025-06-16 00:58:11',
        '2025-06-16 00:58:11',
        0,
        1,
        NULL,
        NULL,
        '-1',
        0,
        NULL);

-- TODO： 这里的库表设计其实是有点问题的, 专业应该关联系部的 id 值而不是直接关联名字...这个数据表严格来说需要重新设计, 不过本次项目要求时间紧急, 暂时不修改, 后续接口编写过程中涉及到这个问题的话我会给出 TODO 注释...等后来着(如果有的话)来修改吧...

