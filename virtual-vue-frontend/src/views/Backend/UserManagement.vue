<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">用户管理</h1>
        <p class="backend-page__subtitle">统一维护用户资料和角色信息，支持搜索与本地分页。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button type="primary" @click="openAddDialog">新增用户</el-button>
        <el-button @click="fetchUsers">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">用户列表</h2>
            <p class="backend-page__section-desc">支持按用户名、邮箱、手机和角色搜索。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchQuery" clearable placeholder="搜索用户" style="width: 240px" @input="resetPage" />
          </div>
        </div>
      </template>

      <el-table :data="pagedUsers" stripe>
        <el-table-column label="用户名" prop="username" min-width="140" />
        <el-table-column label="邮箱" prop="email" min-width="180" />
        <el-table-column label="手机" prop="phone" width="150" />
        <el-table-column label="角色" prop="role" width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="backend-page__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="pageSizes"
          :total="total"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editUserId ? '编辑用户' : '新增用户'" width="520px" class="backend-page__dialog" @close="resetForm">
      <el-form :model="userForm" label-width="96px" class="backend-form">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="用户" value="user" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getAllUsers, updateUser } from '@/api/index.js'
import { useLocalPagination } from '@/composables/useLocalPagination'

const users = ref([])
const searchQuery = ref('')
const dialogVisible = ref(false)
const editUserId = ref(null)
const userForm = ref({ username: '', email: '', phone: '', role: 'user', password: '' })

const filteredUsers = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return users.value
  return users.value.filter((user) =>
    [user.username, user.email, user.phone, user.role].filter(Boolean).join(' ').toLowerCase().includes(keyword)
  )
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedUsers, resetPage } = useLocalPagination(filteredUsers, { pageSize: 10 })

const normalizeList = (response) => response?.data?.data || response?.data || []

const fetchUsers = async () => {
  try {
    const response = await getAllUsers()
    users.value = normalizeList(response)
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const openAddDialog = () => {
  dialogVisible.value = true
  userForm.value = { username: '', email: '', phone: '', role: 'user', password: '' }
  editUserId.value = null
}

const openEditDialog = (user) => {
  dialogVisible.value = true
  userForm.value = { ...user }
  editUserId.value = user.id
}

const submitForm = async () => {
  if (!userForm.value.username || !userForm.value.email) {
    ElMessage.warning('请填写用户名和邮箱')
    return
  }
  try {
    if (editUserId.value) {
      await updateUser(editUserId.value, userForm.value)
    } else {
      await createUser(userForm.value)
    }
    ElMessage.success('用户已保存')
    dialogVisible.value = false
    await fetchUsers()
  } catch (error) {
    ElMessage.error('保存用户失败')
  }
}

const removeUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定删除该用户吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(userId)
    ElMessage.success('用户已删除')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

const resetForm = () => {
  userForm.value = { username: '', email: '', phone: '', role: 'user', password: '' }
  editUserId.value = null
}

onMounted(fetchUsers)
</script>
