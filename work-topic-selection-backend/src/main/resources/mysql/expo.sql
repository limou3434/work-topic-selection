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
    `project` AS 专业
FROM `user`;
