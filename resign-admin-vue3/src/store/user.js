import { defineStore } from 'pinia'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser, 
  batchDeleteUsers,
  toggleUserStatus,
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    userList: [],
    userTotal: 0,
    userLoading: false,
    roleList: [],
    roleTotal: 0,
    roleLoading: false,
    permissionTree: [],
    permissionLoading: false
  }),
  
  actions: {
    // 用户相关
    async fetchUserList(params) {
      this.userLoading = true
      try {
        const response = await getUserList(params)
        // response 是经过拦截器处理的，应该直接包含 data 字段
        const data = response.data || response
        this.userList = data.records || []
        this.userTotal = data.total || 0
        return { data }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.userList = []
        this.userTotal = 0
        return { data: { records: [], total: 0 } }
      } finally {
        this.userLoading = false
      }
    },
    
    async createNewUser(userData) {
      try {
        const { data } = await createUser(userData)
        return data
      } catch (error) {
        console.error('创建用户失败:', error)
        throw error
      }
    },
    
    async updateUserInfo(userId, userData) {
      try {
        const { data } = await updateUser(userId, userData)
        return data
      } catch (error) {
        console.error('更新用户失败:', error)
        throw error
      }
    },
    
    async deleteUserById(userId) {
      try {
        await deleteUser(userId)
        // 从列表中移除
        this.userList = this.userList.filter(user => user.id !== userId)
        this.userTotal--
      } catch (error) {
        console.error('删除用户失败:', error)
        throw error
      }
    },
    
    async batchDeleteUsers(userIds) {
      try {
        await batchDeleteUsers(userIds)
        // 从列表中移除
        this.userList = this.userList.filter(user => !userIds.includes(user.id))
        this.userTotal -= userIds.length
      } catch (error) {
        console.error('批量删除用户失败:', error)
        throw error
      }
    },
    
    async toggleUserStatus(userId, status) {
      try {
        await toggleUserStatus(userId, status)
        // 更新列表中的状态
        const user = this.userList.find(u => u.id === userId)
        if (user) {
          user.status = status
        }
      } catch (error) {
        console.error('切换用户状态失败:', error)
        throw error
      }
    },
    
    // 角色相关
    async fetchRoleList(params) {
      this.roleLoading = true
      try {
        const { data } = await getRoleList(params)
        this.roleList = data.records || []
        this.roleTotal = data.total || 0
        return data
      } catch (error) {
        console.error('获取角色列表失败:', error)
        this.roleList = []
        this.roleTotal = 0
        return { records: [], total: 0 }
      } finally {
        this.roleLoading = false
      }
    },
    
    async createNewRole(roleData) {
      try {
        const { data } = await createRole(roleData)
        return data
      } catch (error) {
        console.error('创建角色失败:', error)
        throw error
      }
    },
    
    async updateRoleInfo(roleId, roleData) {
      try {
        const { data } = await updateRole(roleId, roleData)
        return data
      } catch (error) {
        console.error('更新角色失败:', error)
        throw error
      }
    },
    
    async deleteRoleById(roleId) {
      try {
        await deleteRole(roleId)
        // 从列表中移除
        this.roleList = this.roleList.filter(role => role.id !== roleId)
        this.roleTotal--
      } catch (error) {
        console.error('删除角色失败:', error)
        throw error
      }
    },
    
    // 权限相关
    async fetchPermissionTree() {
      this.permissionLoading = true
      try {
        const { data } = await getPermissionTree()
        this.permissionTree = data || []
        return data
      } catch (error) {
        console.error('获取权限树失败:', error)
        this.permissionTree = []
        return []
      } finally {
        this.permissionLoading = false
      }
    },
    
    async createNewPermission(permissionData) {
      try {
        const { data } = await createPermission(permissionData)
        return data
      } catch (error) {
        console.error('创建权限失败:', error)
        throw error
      }
    },
    
    async updatePermissionInfo(permissionId, permissionData) {
      try {
        const { data } = await updatePermission(permissionId, permissionData)
        return data
      } catch (error) {
        console.error('更新权限失败:', error)
        throw error
      }
    },
    
    async deletePermissionById(permissionId) {
      try {
        await deletePermission(permissionId)
      } catch (error) {
        console.error('删除权限失败:', error)
        throw error
      }
    }
  }
})