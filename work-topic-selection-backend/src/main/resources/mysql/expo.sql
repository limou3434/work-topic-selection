USE `work_topic_selection`;

SELECT
    `userAccount` AS 帐号,
    `userName` AS 姓名,
    CASE `userRole`
        WHEN 3 THEN '管理员'
        WHEN 2 THEN '主任'
        WHEN 1 THEN '教师'
        WHEN 0 THEN '学生'
        END AS 角色,
    `dept` AS 系部,
    `project` AS 专业,
    `topicAmount` AS 出题数量或预选数量
FROM `user`;

USE `work_topic_selection`;

SELECT
    `userAccount` AS 帐号,
    `userName` AS 姓名,
    CASE `userRole`
        WHEN 3 THEN '管理员'
        WHEN 2 THEN '主任'
        WHEN 1 THEN '教师'
        WHEN 0 THEN '学生'
        END AS 角色,
    `dept` AS 系部,
    `project` AS 专业,
    `topicAmount` AS 当前剩余出题数量
FROM `user` WHERE `userRole` = 1 AND `isDelete` = 0 AND `topicAmount` > 0;

USE `work_topic_selection`;

SELECT
    u.userName AS teacherName,
    COUNT(t.id) + u.topicAmount AS totalTopics
FROM
    user u
        LEFT JOIN
    topic t ON u.userName = t.teacherName AND t.isDelete = 0
WHERE
    u.userRole = 1  -- 教师角色
  AND u.isDelete = 0
GROUP BY
    u.userName, u.topicAmount;
