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

/** addCount POST /user/addCount */
export async function addCountUsingPost(
  body: API.AddCountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/addCount', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addDept POST /user/addDept */
export async function addDeptUsingPost(body: API.DeptAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/user/addDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addProject POST /user/addProject */
export async function addProjectUsingPost(
  body: API.ProjectAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/addProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addTopic POST /user/addTopic */
export async function addTopicUsingPost(
  body: API.AddTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/addTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** checkTopic POST /user/CheckTopic */
export async function checkTopicUsingPost(
  body: API.CheckTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/CheckTopic', {
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

/** deleteDept POST /user/deleteDept */
export async function deleteDeptUsingPost(
  body: API.DeleteDeptRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/deleteDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteProject POST /user/deleteProject */
export async function deleteProjectUsingPost(
  body: API.DeleteProjectRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/deleteProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteTopic POST /user/deleteTopic */
export async function deleteTopicUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/deleteTopic', {
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

/** getLoginUser GET /user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO_>('/user/get/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getUserVOById GET /user/get/vo */
export async function getUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserVO_>('/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getDept POST /user/getDept */
export async function getDeptUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDept_>('/user/getDept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getDeptList POST /user/getDeptList */
export async function getDeptListUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListDeptVO_>('/user/getDeptList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /user/getDeptTeacher */
export async function getTeacherUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/getDeptTeacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacherByAdmin POST /user/getDeptTeacherByAdmin */
export async function getTeacherByAdminUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/getDeptTeacherByAdmin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getPreTopic POST /user/getPreTopic */
export async function getPreTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/getPreTopic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getProject POST /user/getProject */
export async function getProjectUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageProject_>('/user/getProject', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getProjectList POST /user/getProjectList */
export async function getProjectListUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListProjectVO_>('/user/getProjectList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopic POST /user/getSelectTopic */
export async function getSelectTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/getSelectTopic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getSelectTopicById POST /user/getSelectTopicById */
export async function getSelectTopicByIdUsingPost(
  body: API.GetSelectTopicById,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/getSelectTopicById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopicSituation POST /user/getSelectTopicSituation */
export async function getSelectTopicSituationUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseSituationVO_>('/user/getSelectTopicSituation', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getStudentByTopicId POST /user/getStudentByTopicId */
export async function getStudentByTopicIdUsingPost(
  body: API.GetStudentByTopicId,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/getStudentByTopicId', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /user/getTeacher */
export async function getTeacherUsingPost1(
  body: API.TeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListTeacherVO_>('/user/getTeacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicList POST /user/getTopicList */
export async function getTopicListUsingPost(
  body: API.TopicQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/getTopicList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicListByAdmin POST /user/getTopicListByAdmin */
export async function getTopicListByAdminUsingPost(
  body: API.TopicQueryByAdminRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/getTopicListByAdmin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUnSelectTopicStudentList POST /user/getUnSelectTopicStudentList */
export async function getUnSelectTopicStudentListUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUser_>('/user/getUnSelectTopicStudentList', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getUserList POST /user/getUserList */
export async function getUserListUsingPost(
  body: API.GetUserListRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUserNameVO_>('/user/getUserList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserByPage POST /user/list/page */
export async function listUserByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUser_>('/user/list/page', {
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

/** preSelectTopicById POST /user/preSelectTopicById */
export async function preSelectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/preSelectTopicById', {
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

/** resetPassword POST /user/resetPassword */
export async function resetPasswordUsingPost(
  body: API.ResetPasswordRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/resetPassword', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectStudent POST /user/selectStudent */
export async function selectStudentUsingPost(
  body: API.SelectStudentRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/selectStudent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectTopicById POST /user/selectTopicById */
export async function selectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/selectTopicById', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setTimeById POST /user/setTimeById */
export async function setTimeByIdUsingPost(
  body: API.SetTimeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/setTimeById', {
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

/** updateTopic POST /user/updateTopic */
export async function updateTopicUsingPost(
  body: API.UpdateTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/updateTopic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** Withdraw POST /user/Withdraw */
export async function withdrawUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/Withdraw', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
