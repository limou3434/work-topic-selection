// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** addUser POST /api/user/add */
export async function addUserUsingPost(body: API.UserAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/api/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addCount POST /api/user/addCount */
export async function addCountUsingPost(
  body: API.AddCountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/user/addCount', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addDept POST /api/user/addDept */
export async function addDeptUsingPost(body: API.DeptAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/api/user/addDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addProject POST /api/user/addProject */
export async function addProjectUsingPost(
  body: API.ProjectAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/addProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addTopic POST /api/user/addTopic */
export async function addTopicUsingPost(
  body: API.AddTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/addTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** checkTopic POST /api/user/CheckTopic */
export async function checkTopicUsingPost(
  body: API.CheckTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/CheckTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteUser POST /api/user/delete */
export async function deleteUserUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteDept POST /api/user/deleteDept */
export async function deleteDeptUsingPost(
  body: API.DeleteDeptRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/deleteDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteProject POST /api/user/deleteProject */
export async function deleteProjectUsingPost(
  body: API.DeleteProjectRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/deleteProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteTopic POST /api/user/deleteTopic */
export async function deleteTopicUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/deleteTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUserById GET /api/user/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUser_>('/api/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getLoginUser GET /api/user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO_>('/api/user/get/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getUserVOById GET /api/user/get/vo */
export async function getUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserVO_>('/api/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getDept POST /api/user/getDept */
export async function getDeptUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDept_>('/api/user/getDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getDeptList POST /api/user/getDeptList */
export async function getDeptListUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListDeptVO_>('/api/user/getDeptList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /api/user/getDeptTeacher */
export async function getTeacherUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/api/user/getDeptTeacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacherByAdmin POST /api/user/getDeptTeacherByAdmin */
export async function getTeacherByAdminUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/api/user/getDeptTeacherByAdmin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getPreTopic POST /api/user/getPreTopic */
export async function getPreTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/api/user/getPreTopic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getProject POST /api/user/getProject */
export async function getProjectUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageProject_>('/api/user/getProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getProjectList POST /api/user/getProjectList */
export async function getProjectListUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListProjectVO_>('/api/user/getProjectList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopic POST /api/user/getSelectTopic */
export async function getSelectTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/api/user/getSelectTopic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getSelectTopicById POST /api/user/getSelectTopicById */
export async function getSelectTopicByIdUsingPost(
  body: API.GetSelectTopicById,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/api/user/getSelectTopicById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopicSituation POST /api/user/getSelectTopicSituation */
export async function getSelectTopicSituationUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseSituationVO_>('/api/user/getSelectTopicSituation', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getStudentByTopicId POST /api/user/getStudentByTopicId */
export async function getStudentByTopicIdUsingPost(
  body: API.GetStudentByTopicId,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/api/user/getStudentByTopicId', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /api/user/getTeacher */
export async function getTeacherUsingPost1(
  body: API.TeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListTeacherVO_>('/api/user/getTeacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicList POST /api/user/getTopicList */
export async function getTopicListUsingPost(
  body: API.TopicQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/api/user/getTopicList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicListByAdmin POST /api/user/getTopicListByAdmin */
export async function getTopicListByAdminUsingPost(
  body: API.TopicQueryByAdminRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/api/user/getTopicListByAdmin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUnSelectTopicStudentList POST /api/user/getUnSelectTopicStudentList */
export async function getUnSelectTopicStudentListUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUser_>('/api/user/getUnSelectTopicStudentList', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getUserList POST /api/user/getUserList */
export async function getUserListUsingPost(
  body: API.GetUserListRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUserNameVO_>('/api/user/getUserList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserByPage POST /api/user/list/page */
export async function listUserByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUser_>('/api/user/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserVOByPage POST /api/user/list/page/vo */
export async function listUserVoByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO_>('/api/user/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogin POST /api/user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/api/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogout POST /api/user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}

/** preSelectTopicById POST /api/user/preSelectTopicById */
export async function preSelectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/preSelectTopicById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userUpdatePassword POST /api/user/register */
export async function userUpdatePasswordUsingPost(
  body: API.UserUpdatePassword,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** resetPassword POST /api/user/resetPassword */
export async function resetPasswordUsingPost(
  body: API.ResetPasswordRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/user/resetPassword', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectStudent POST /api/user/selectStudent */
export async function selectStudentUsingPost(
  body: API.SelectStudentRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/user/selectStudent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectTopicById POST /api/user/selectTopicById */
export async function selectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/selectTopicById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setTimeById POST /api/user/setTimeById */
export async function setTimeByIdUsingPost(
  body: API.SetTimeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/user/setTimeById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateUser POST /api/user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateMyUser POST /api/user/update/my */
export async function updateMyUserUsingPost(
  body: API.UserUpdateMyRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/update/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateTopic POST /api/user/updateTopic */
export async function updateTopicUsingPost(
  body: API.UpdateTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/api/user/updateTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** Withdraw POST /api/user/Withdraw */
export async function withdrawUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/api/user/Withdraw', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
