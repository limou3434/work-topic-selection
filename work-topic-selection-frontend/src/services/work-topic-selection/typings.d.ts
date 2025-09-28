declare namespace API {
  type AddCountRequest = {
    count?: number;
    id?: number;
  };

  type AddTopicRequest = {
    amount?: number;
    deptName?: string;
    deptTeacher?: string;
    description?: string;
    requirement?: string;
    teacherName?: string;
    topic?: string;
    type?: string;
  };

  type AIResult = {
    description?: string;
    level?: string;
  };

  type BaseResponseAIResult_ = {
    code?: number;
    data?: AIResult;
    message?: string;
  };

  type BaseResponseBoolean_ = {
    code?: number;
    data?: boolean;
    message?: string;
  };

  type BaseResponseInt_ = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponseListDeptVO_ = {
    code?: number;
    data?: DeptVO[];
    message?: string;
  };

  type BaseResponseListProjectVO_ = {
    code?: number;
    data?: ProjectVO[];
    message?: string;
  };

  type BaseResponseListTeacherVO_ = {
    code?: number;
    data?: TeacherVO[];
    message?: string;
  };

  type BaseResponseListTopic_ = {
    code?: number;
    data?: Topic[];
    message?: string;
  };

  type BaseResponseListUser_ = {
    code?: number;
    data?: User[];
    message?: string;
  };

  type BaseResponseListUserNameVO_ = {
    code?: number;
    data?: UserNameVO[];
    message?: string;
  };

  type BaseResponseLoginUserVO_ = {
    code?: number;
    data?: LoginUserVO;
    message?: string;
  };

  type BaseResponseLong_ = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponsePageDept_ = {
    code?: number;
    data?: PageDept_;
    message?: string;
  };

  type BaseResponsePageDeptTeacherVO_ = {
    code?: number;
    data?: PageDeptTeacherVO_;
    message?: string;
  };

  type BaseResponsePageProject_ = {
    code?: number;
    data?: PageProject_;
    message?: string;
  };

  type BaseResponsePageTopic_ = {
    code?: number;
    data?: PageTopic_;
    message?: string;
  };

  type BaseResponsePageUser_ = {
    code?: number;
    data?: PageUser_;
    message?: string;
  };

  type BaseResponsePageUserVO_ = {
    code?: number;
    data?: PageUserVO_;
    message?: string;
  };

  type BaseResponseSituationVO_ = {
    code?: number;
    data?: SituationVO;
    message?: string;
  };

  type BaseResponseString_ = {
    code?: number;
    data?: string;
    message?: string;
  };

  type BaseResponseUser_ = {
    code?: number;
    data?: User;
    message?: string;
  };

  type BaseResponseUserVO_ = {
    code?: number;
    data?: UserVO;
    message?: string;
  };

  type CaptchaRequest = {
    email?: string;
  };

  type CheckCaptchaRequest = {
    captcha?: string;
    email?: string;
  };

  type CheckTopicRequest = {
    id?: number;
    reason?: string;
    status?: number;
  };

  type DeleteDeptRequest = {
    deptName?: string;
  };

  type DeleteProjectRequest = {
    projectName?: string;
  };

  type DeleteRequest = {
    userAccount?: string;
  };

  type DeleteTopicRequest = {
    id?: number;
  };

  type Dept = {
    createTime?: string;
    deptName?: string;
    id?: number;
    isDelete?: number;
    updateTime?: string;
  };

  type DeptAddRequest = {
    deptName?: string;
  };

  type DeptQueryRequest = {
    current?: number;
    deptName?: string;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
  };

  type DeptTeacherQueryRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    teacherName?: string;
  };

  type DeptTeacherVO = {
    selectAmount?: number;
    surplusQuantity?: number;
    teacherName?: string;
    topicAmount?: number;
  };

  type DeptVO = {
    label?: string;
    value?: string;
  };

  type GetSelectTopicById = {
    id?: number;
  };

  type GetStudentByTopicId = {
    id?: number;
  };

  type GetTeacherTopicAmountRequest = {
    teacherId?: number;
  };

  type GetTopicReviewLevelRequest = {
    deptName?: string;
    deptTeacher?: string;
    description?: string;
    requirement?: string;
    topic?: string;
    type?: string;
  };

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type GetUserListRequest = {
    userRole?: number;
  };

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type LoginUserVO = {
    createTime?: string;
    dept?: string;
    email?: string;
    id?: number;
    project?: string;
    updateTime?: string;
    userAvatar?: string;
    userName?: string;
    userRole?: number;
  };

  type OrderItem = {
    asc?: boolean;
    column?: string;
  };

  type PageDept_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: Dept[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageDeptTeacherVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: DeptTeacherVO[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageProject_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: Project[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageTopic_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: Topic[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageUser_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: User[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageUserVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: UserVO[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type Project = {
    createTime?: string;
    deptName?: string;
    id?: number;
    isDelete?: number;
    projectName?: string;
    updateTime?: string;
  };

  type ProjectAddRequest = {
    deptName?: string;
    projectName?: string;
  };

  type ProjectQueryRequest = {
    current?: number;
    deptName?: string;
    pageSize?: number;
    projectName?: string;
    sortField?: string;
    sortOrder?: string;
  };

  type ProjectVO = {
    label?: string;
    value?: string;
  };

  type ResetPasswordRequest = {
    userAccount?: string;
    userName?: string;
  };

  type SelectStudentRequest = {
    topic?: string;
    userAccount?: string;
  };

  type SelectTopicByIdRequest = {
    id?: number;
    status?: number;
  };

  type SendCodeRequest = {
    userAccount?: string;
  };

  type setCrossTopicStatusUsingPOSTParams = {
    /** enabled */
    enabled: boolean;
  };

  type setSwitchSingleChoiceStatusUsingPOSTParams = {
    /** enabled */
    enabled: boolean;
  };

  type SetTeacherTopicAmountRequest = {
    teacherId?: number;
    topicAmount?: number;
  };

  type SetTimeRequest = {
    endTime?: string;
    startTime?: string;
    topicList?: Topic[];
  };

  type SituationVO = {
    amount?: number;
    selectAmount?: number;
    unselectAmount?: number;
  };

  type TeacherQueryRequest = {
    userRole?: number;
  };

  type TeacherVO = {
    label?: string;
    value?: string;
  };

  type Topic = {
    createTime?: string;
    deptName?: string;
    deptTeacher?: string;
    description?: string;
    endTime?: string;
    id?: number;
    isDelete?: number;
    reason?: string;
    requirement?: string;
    selectAmount?: number;
    startTime?: string;
    status?: number;
    surplusQuantity?: number;
    teacherName?: string;
    topic?: string;
    type?: string;
    updateTime?: string;
  };

  type TopicQueryByAdminRequest = {
    current?: number;
    deptName?: string;
    endTime?: string;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    startTime?: string;
    teacherName?: string;
    topic?: string;
    type?: string;
  };

  type TopicQueryRequest = {
    current?: number;
    deptName?: string;
    endTime?: string;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    startTime?: string;
    status?: number;
    teacherName?: string;
    topic?: string;
    type?: string;
  };

  type UnSetTimeRequest = {
    topicList?: Topic[];
  };

  type UpdateTopicListRequest = {
    deptName?: string;
    deptTeacher?: string;
    description?: string;
    id?: number;
    requirement?: string;
    teacherName?: string;
    topic?: string;
    type?: string;
  };

  type UpdateTopicRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    updateTopicListRequests?: UpdateTopicListRequest[];
  };

  type uploadFileUsingPOSTParams = {
    status?: number;
  };

  type User = {
    createTime?: string;
    dept?: string;
    email?: string;
    id?: number;
    isDelete?: number;
    project?: string;
    status?: string;
    topicAmount?: number;
    updateTime?: string;
    userAccount?: string;
    userName?: string;
    userPassword?: string;
    userRole?: number;
  };

  type UserAddRequest = {
    deptName?: string;
    project?: string;
    userAccount?: string;
    userName?: string;
    userRole?: number;
  };

  type UserLoginRequest = {
    userAccount?: string;
    userPassword?: string;
  };

  type UserNameVO = {
    userName?: string;
  };

  type UserQueryRequest = {
    current?: number;
    dept?: string;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    userAccount?: string;
    userName?: string;
    userRole?: number;
  };

  type UserToggleRequest = {
    userRole?: number;
  };

  type UserUpdatePassword = {
    code?: string;
    email?: string;
    updatePassword?: string;
    userAccount?: string;
    userPassword?: string;
  };

  type UserUpdateRequest = {
    id?: number;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserVO = {
    createTime?: string;
    id?: number;
    status?: string;
    userAvatar?: string;
    userName?: string;
    userRole?: number;
  };
}
