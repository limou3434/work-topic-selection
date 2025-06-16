// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** addUser POST /user/add */
export async function addUserUsingPost(body: API.UserAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addCount POST /user/add/count */
export async function addCountUsingPost(
  body: API.AddCountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/add/count', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addDept POST /user/add/dept */
export async function addDeptUsingPost(body: API.DeptAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/user/add/dept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addProject POST /user/add/project */
export async function addProjectUsingPost(
  body: API.ProjectAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/add/project', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addTopic POST /user/add/topic */
export async function addTopicUsingPost(
  body: API.AddTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/add/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** checkTopic POST /user/check/topic */
export async function checkTopicUsingPost(
  body: API.CheckTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/check/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteUser POST /user/delete */
export async function deleteUserUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteDept POST /user/delete/dept */
export async function deleteDeptUsingPost(
  body: API.DeleteDeptRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/dept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteProject POST /user/delete/project */
export async function deleteProjectUsingPost(
  body: API.DeleteProjectRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/project', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteTopic POST /user/delete/topic */
export async function deleteTopicUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUserById GET /user/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUser_>('/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getDeptList POST /user/get/dept/list */
export async function getDeptListUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListDeptVO_>('/user/get/dept/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getDept POST /user/get/dept/page */
export async function getDeptUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDept_>('/user/get/dept/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /user/get/dept/teacher */
export async function getTeacherUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/get/dept/teacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacherByAdmin POST /user/get/dept/teacher/by/admin */
export async function getTeacherByAdminUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/get/dept/teacher/by/admin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getLoginUser GET /user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO_>('/user/get/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getPreTopic POST /user/get/pre/topic */
export async function getPreTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/get/pre/topic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getProjectList POST /user/get/project/list */
export async function getProjectListUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListProjectVO_>('/user/get/project/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getProject POST /user/get/project/page */
export async function getProjectUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageProject_>('/user/get/project/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopic POST /user/get/select/topic */
export async function getSelectTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/get/select/topic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getSelectTopicById POST /user/get/select/topic/by/id */
export async function getSelectTopicByIdUsingPost(
  body: API.GetSelectTopicById,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/get/select/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopicSituation POST /user/get/select/topic/situation */
export async function getSelectTopicSituationUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseSituationVO_>('/user/get/select/topic/situation', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getStudentByTopicId POST /user/get/student/by/topicId */
export async function getStudentByTopicIdUsingPost(
  body: API.GetStudentByTopicId,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/get/student/by/topicId', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /user/get/teacher */
export async function getTeacherUsingPost1(
  body: API.TeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListTeacherVO_>('/user/get/teacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicListByAdmin POST /user/get/topic/list/by/admin */
export async function getTopicListByAdminUsingPost(
  body: API.TopicQueryByAdminRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/get/topic/list/by/admin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicList POST /user/get/topic/page */
export async function getTopicListUsingPost(
  body: API.TopicQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/get/topic/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUnSelectTopicStudentList POST /user/get/un/select/topic/student/list */
export async function getUnSelectTopicStudentListUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUser_>('/user/get/un/select/topic/student/list', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getUserList POST /user/get/user/list */
export async function getUserListUsingPost(
  body: API.GetUserListRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUserNameVO_>('/user/get/user/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserByPage POST /user/get/user/page */
export async function listUserByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUser_>('/user/get/user/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserVOByPage POST /user/list/page/vo */
export async function listUserVoByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO_>('/user/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogin POST /user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogout POST /user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}

/** preSelectTopicById POST /user/pre/select/topic/by/id */
export async function preSelectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/pre/select/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userUpdatePassword POST /user/register */
export async function userUpdatePasswordUsingPost(
  body: API.UserUpdatePassword,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** resetPassword POST /user/reset/password */
export async function resetPasswordUsingPost(
  body: API.ResetPasswordRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/reset/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectStudent POST /user/select/student */
export async function selectStudentUsingPost(
  body: API.SelectStudentRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/select/student', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectTopicById POST /user/select/topic/by/id */
export async function selectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/select/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setTimeById POST /user/set/time/by/id */
export async function setTimeByIdUsingPost(
  body: API.SetTimeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/set/time/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateUser POST /user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateMyUser POST /user/update/my */
export async function updateMyUserUsingPost(
  body: API.UserUpdateMyRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/update/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateTopic POST /user/update/topic */
export async function updateTopicUsingPost(
  body: API.UpdateTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/update/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** Withdraw POST /user/withdraw */
export async function withdrawUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/withdraw', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
