export default [
  {
    name: '登录',
    path: '/user',
    layout: false,
    routes: [{ path: '/user/login', component: './User/Login' }],
  },
  {
    name: '修改密码',
    path: '/user',
    layout: false,
    routes: [{ path: '/user/register', component: './User/updatePassword' }],
  },
  { path: '/user', redirect: '/home' },
  { path: '/', redirect: '/home' }, // 根路径也重定向到主页

  {
    name: '主页',
    path: '/home',
    icon: 'HomeOutlined',
    component: './Homepage',
  },
  {
    name: '管理',
    path: '/admin',
    icon: 'FormOutlined',
    routes: [
      {
        name: '系统账号管理',
        path: '/admin/admins',
        component: './AdminList',
        access: 'canAccessAdmin',
      },
      {
        name: '院系系部管理',
        path: '/admin/dept',
        component: './DeptList',
        access: 'canAccessAdmin',
      },
      {
        name: '系部专业管理',
        path: '/admin/project',
        component: './ProjectList',
        access: 'canAccessAdmin',
      },
      {
        name: '主任账号管理',
        path: '/admin/deptTeacher',
        component: './DeptTeacherList',
        access: 'canAccessAdmin',
      },
      {
        name: '教师账号管理',
        path: '/admin/teacher',
        component: './TeacherList',
        access: 'canAccessAdmin',
      },
      {
        name: '学生账号管理',
        path: '/admin/student',
        component: './StudentList',
        access: 'canAccessAdmin',
      },
    ],
  },
  {
    name: '毕业设计题目管理',
    path: '/topic',
    icon: 'BarChartOutlined',
    routes: [
      {
        name: '发布及修改题目',
        path: '/topic/teacher',
        component: './TeacherPublicTopic',
        access: 'canAccessTeacher',
      },
    ],
  },
  {
    path: '/topic/teacher/selectStudent/:topic',
    component: './TeacherPublicTopic/studentList',
    access: 'canAccessTeacher',
  },
  {
    path: '/setTopicTime/:id',
    component: './setTopicTime',
  },
  {
    name: '选题',
    path: '/schedule',
    icon: 'FieldTimeOutlined',
    component: './setTopicTime',
    access: 'canAccessNotStudentAndTeacher',
  },
  {
    name: '审核题目',
    path: '/check',
    icon: 'CheckSquareOutlined',
    component: './CheckTopic/index',
    access: 'canAccessDept',
  },
  {
    name: '学生选题',
    path: '/select/student',
    icon: 'BarChartOutlined',
    routes: [
      {
        name: '选择选题',
        path: '/select/student/select',
        component: './studentSelection',
        access: 'canAccessStudent',
      },
      {
        name: '提交选题',
        path: '/select/student/submit',
        component: './SubmitTopic',
        access: 'canAccessStudent',
      },
      {
        name: '查看选题',
        path: '/select/student/view',
        component: './ViewTopic',
        access: 'canAccessStudent',
      },
    ],
  },
  {
    path: '/topic/student/view/:id',
    component: './SelectStudentList',
  },
  {
    path: '/topic/student/select/:teacherName',
    component: './studentSelection/topic',
  },
  {
    name: '查看',
    path: '/topic/view',
    icon: 'BarChartOutlined',
    routes: [
      {
        name: '查看选题',
        path: '/topic/view/topic',
        component: './ViewStudentTopic',
        access: 'canAccessNotStudent',
      },
      {
        name: '选题情况',
        path: '/topic/view/SelectTopicSituation',
        component: './SelectTopicSituation',
        access: 'canAccessAdmin',
      },
      {
        name: '选题情况',
        path: '/topic/view/SelectTopicSituationToDept',
        component: './SelectTopicSituationToDept',
        access: 'canAccessDept',
      },
    ],
  },
  {
    path: '/topic/view/SelectTopicSituation/student',
    component: './SelectTopicSituation/studentList',
    access: 'canAccessNotStudentAndTeacher',
  },
  {
    path: '/topic/view/SelectTopicSituation/topic/:userAccount',
    component: './SelectTopicSituation/topic',
    access: 'canAccessNotStudentAndTeacher',
  },
  { path: '*', redirect: '/home' }, // 通配符路径重定向到主页
];
