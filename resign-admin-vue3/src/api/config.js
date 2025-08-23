import request from '@/utils/request'

// 获取重签名配置
export function getResignConfig() {
  return request({
    url: '/api/config/resign',
    method: 'get'
  })
}

// 保存重签名配置
export function saveResignConfig(data) {
  return request({
    url: '/api/config/resign',
    method: 'post',
    data
  })
}

// 获取MinIO配置
export function getMinioConfig() {
  return request({
    url: '/api/config/minio',
    method: 'get'
  })
}

// 保存MinIO配置
export function saveMinioConfig(data) {
  return request({
    url: '/api/config/minio',
    method: 'post',
    data
  })
}

// 测试MinIO连接
export function testMinioConnection(data) {
  return request({
    url: '/api/config/minio/test',
    method: 'post',
    data
  })
}

// 获取RabbitMQ配置
export function getRabbitConfig() {
  return request({
    url: '/api/config/rabbitmq',
    method: 'get'
  })
}

// 保存RabbitMQ配置
export function saveRabbitConfig(data) {
  return request({
    url: '/api/config/rabbitmq',
    method: 'post',
    data
  })
}

// 测试RabbitMQ连接
export function testRabbitConnection(data) {
  return request({
    url: '/api/config/rabbitmq/test',
    method: 'post',
    data
  })
}

// 获取系统信息
export function getSystemInfo() {
  return request({
    url: '/api/system/info',
    method: 'get'
  })
}