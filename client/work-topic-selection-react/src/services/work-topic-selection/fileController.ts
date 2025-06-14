// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** getSelectTopicStudentListCsv POST /file/getSelectTopicStudentList */
export async function getSelectTopicStudentListCsvUsingPost(options?: { [key: string]: any }) {
  return request<any>('/file/getSelectTopicStudentList', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getUnSelectTopicStudentListCsv POST /file/getUnSelectTopicStudentList */
export async function getUnSelectTopicStudentListCsvUsingPost(options?: { [key: string]: any }) {
  return request<any>('/file/getUnSelectTopicStudentList', {
    method: 'POST',
    ...(options || {}),
  });
}

/** uploadFile POST /file/upload */
export async function uploadFileUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadFileUsingPOSTParams,
  body: {},
  file?: File,
  options?: { [key: string]: any },
) {
  const formData = new FormData();

  if (file) {
    formData.append('file', file);
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele];

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''));
        } else {
          formData.append(ele, JSON.stringify(item));
        }
      } else {
        formData.append(ele, item);
      }
    }
  });

  return request<API.BaseResponseString_>('/file/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  });
}

/** uploadFileTopic POST /file/uploadTopic */
export async function uploadFileTopicUsingPost(
  body: {},
  file?: File,
  options?: { [key: string]: any },
) {
  const formData = new FormData();

  if (file) {
    formData.append('file', file);
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele];

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''));
        } else {
          formData.append(ele, JSON.stringify(item));
        }
      } else {
        formData.append(ele, item);
      }
    }
  });

  return request<API.BaseResponseString_>('/file/uploadTopic', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  });
}
