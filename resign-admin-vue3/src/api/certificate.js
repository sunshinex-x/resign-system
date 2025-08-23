import request from '@/utils/request'

// 获取证书统计信息
export function getCertificateStats() {
  return request({
    url: '/api/certificate/stats',
    method: 'get'
  })
}

// 获取最近证书列表
export function getRecentCertificates(params) {
  return request({
    url: '/api/certificate/recent',
    method: 'get',
    params
  })
}

// iOS证书相关API

// 获取iOS证书列表
export function getIosCertificateList(params) {
  return request({
    url: '/api/ios/certificate/list',
    method: 'get',
    params
  })
}

// 上传iOS证书
export function uploadIosCertificate(data) {
  return request({
    url: '/api/ios/certificate/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取iOS证书详情
export function getIosCertificateDetail(id) {
  return request({
    url: `/api/ios/certificate/${id}`,
    method: 'get'
  })
}

// 验证iOS证书
export function validateIosCertificate(id) {
  return request({
    url: `/api/ios/certificate/${id}/validate`,
    method: 'post'
  })
}

// 删除iOS证书
export function deleteIosCertificate(id) {
  return request({
    url: `/api/ios/certificate/${id}`,
    method: 'delete'
  })
}

// 获取证书关联的Profile列表
export function getCertificateProfiles(certificateId) {
  return request({
    url: `/api/ios/certificate/${certificateId}/profiles`,
    method: 'get'
  })
}

// 下载Profile文件
export function downloadProfile(profileId) {
  return request({
    url: `/api/ios/profiles/${profileId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// Android证书相关API

// 获取Android证书列表
export function getAndroidCertificateList(params) {
  return request({
    url: '/api/android/certificate/list',
    method: 'get',
    params
  })
}

// 上传Android证书
export function uploadAndroidCertificate(data) {
  return request({
    url: '/api/android/certificate/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取Android证书详情
export function getAndroidCertificateDetail(id) {
  return request({
    url: `/api/android/certificate/${id}`,
    method: 'get'
  })
}

// 验证Android证书
export function validateAndroidCertificate(id) {
  return request({
    url: `/api/android/certificate/${id}/validate`,
    method: 'post'
  })
}

// 删除Android证书
export function deleteAndroidCertificate(id) {
  return request({
    url: `/api/android/certificate/${id}`,
    method: 'delete'
  })
}

// 通用证书API（兼容旧版本）

// 获取证书列表
export function getCertificateList(params) {
  return request({
    url: '/api/certificate/certificates',
    method: 'get',
    params
  })
}

// 上传证书
export function uploadCertificate(data) {
  return request({
    url: '/api/certificate/upload-certificate',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 验证证书
export function validateCertificate(id, params) {
  return request({
    url: `/api/certificate/validate-certificate/${id}`,
    method: 'post',
    params
  })
}

// 删除证书
export function deleteCertificate(id) {
  return request({
    url: `/api/certificate/certificates/${id}`,
    method: 'delete'
  })
}