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
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    `userRole`     INT          NOT NULL DEFAULT 0 COMMENT '用户角色 0-学生 1-教师 2-主任 3-系统',
    `dept`         VARCHAR(256)          DEFAULT NULL COMMENT '系部',
    `status`       VARCHAR(256)          DEFAULT NULL COMMENT '账号状态',
    `project`      VARCHAR(256)          DEFAULT NULL COMMENT '专业',
    `topicAmount`  INT                   DEFAULT NULL COMMENT '预先选题数量/最大出题数量',
    `email`        VARCHAR(256)          DEFAULT NULL COMMENT '验证码发送邮箱',
    PRIMARY KEY (`id`) USING BTREE COMMENT '主键',
    UNIQUE KEY (`userAccount`) COMMENT '唯一键',
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
    `isDelete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
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
    `isDelete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
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
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `topic`           VARCHAR(255)      DEFAULT NULL COMMENT '题目',
    `type`            VARCHAR(255)      DEFAULT NULL COMMENT '题目类型',
    `description`     LONGTEXT COMMENT '题目描述',
    `requirement`     LONGTEXT COMMENT '对学生要求',
    `teacherName`     VARCHAR(256)      DEFAULT NULL COMMENT '指导老师',
    `deptName`        VARCHAR(256)      DEFAULT NULL COMMENT '系部名',
    `deptTeacher`     VARCHAR(256)      DEFAULT NULL COMMENT '系部主任',
    `createTime`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`        TINYINT  NOT NULL DEFAULT 0 COMMENT '是否删除',
    `surplusQuantity` INT      NOT NULL DEFAULT 1 COMMENT '剩余数量',
    `startTime`       DATETIME          DEFAULT NULL COMMENT '开启时间',
    `endTime`         DATETIME          DEFAULT NULL COMMENT '开始时间',
    `status`          INT      NOT NULL DEFAULT -1 COMMENT '是否发布, -2 - 被打回, -1 - 待审核, 1 - 已发布, 0 - 没发布',
    `selectAmount`    INT               DEFAULT 0 COMMENT '预选人数',
    `reason`          VARCHAR(256)      DEFAULT NULL COMMENT '打回理由',
    PRIMARY KEY (`id`) USING BTREE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = `utf8mb3`
    ROW_FORMAT = DYNAMIC COMMENT = '选题表'
;

CREATE TABLE `student_topic_selection`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
    `topicId`     BIGINT       NOT NULL COMMENT '题目 id',
    `createTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    TINYINT      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `status`      INT          NOT NULL DEFAULT '0' COMMENT '选题状态: -1-取消预选, 0-确认预选, 1-取消选题, 2-确认选题',
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
VALUES
    -- 管理帐号
    (1,
     'super-admin',
     '超级管理员',
     'bde672b1737093d5f9ad55ccc26f32eb',
     '2025-06-15 13:25:49',
     '2025-06-15 13:25:49',
     0,
     3,
     NULL,
     '老用户',
     NULL,
     NULL,
     '898738804@qq.com'
     ),
    (2,
     'admin-1',
     '管理员1',
     'bde672b1737093d5f9ad55ccc26f32eb',
     '2025-06-15 13:25:49',
     '2025-06-15 13:25:49',
     0,
     3,
     NULL,
     '老用户',
     NULL,
     NULL,
     '898738804@qq.com'
    ),
    (3,
     'admin-2',
     '管理员2',
     'bde672b1737093d5f9ad55ccc26f32eb',
     '2025-06-15 13:25:49',
     '2025-06-15 13:25:49',
     0,
     3,
     NULL,
     '老用户',
     NULL,
     NULL,
     '898738804@qq.com'
    )
;
