import request from '@/utils/request'

// 获取用户列表（分页）
export function getUserList(params) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(userId) {
  return request({
    url: `/api/user/${userId}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(userId, data) {
  return request({
    url: `/api/user/${userId}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: `/api/user/${userId}`,
    method: 'delete'
  })
}

// 批量删除用户
export function batchDeleteUsers(userIds) {
  return request({
    url: '/api/user/batch',
    method: 'delete',
    data: userIds
  })
}

// 切换用户状态
export function toggleUserStatus(userId, status) {
  return request({
    url: `/api/user/${userId}/status`,
    method: 'put',
    data: { status }
  })
}

// 重置用户密码
export function resetUserPassword(userId, password) {
  return request({
    url: `/api/user/${userId}/password`,
    method: 'put',
    data: { password }
  })
}

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: '/api/role/list',
    method: 'get',
    params
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/api/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(roleId, data) {
  return request({
    url: `/api/role/${roleId}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(roleId) {
  return request({
    url: `/api/role/${roleId}`,
    method: 'delete'
  })
}

// 获取角色权限
export function getRolePermissions(roleId) {
  return request({
    url: `/api/role/${roleId}/permissions`,
    method: 'get'
  })
}

// 设置角色权限
export function setRolePermissions(roleId, permissionIds) {
  return request({
    url: `/api/role/${roleId}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
}

// 获取权限列表
export function getPermissionList(params) {
  return request({
    url: '/api/permission/list',
    method: 'get',
    params
  })
}

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/api/permission/tree',
    method: 'get'
  })
}

// 创建权限
export function createPermission(data) {
  return request({
    url: '/api/permission',
    method: 'post',
    data
  })
}

// 更新权限
export function updatePermission(permissionId, data) {
  return request({
    url: `/api/permission/${permissionId}`,
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(permissionId) {
  return request({
    url: `/api/permission/${permissionId}`,
    method: 'delete'
  })
}