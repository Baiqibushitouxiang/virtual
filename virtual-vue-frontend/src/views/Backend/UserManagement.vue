<template>
  <div class="user-management">
    <!-- 搜索框 -->
    <el-row class="header-row" type="flex" justify="space-between" align="middle">
      <el-input
          v-model="searchQuery"
          placeholder="搜索用户名"
          class="search-input"
          prefix-icon="el-icon-search"
          @input="searchUser"
      />
      <el-button type="primary" @click="openAddDialog" class="add-user-button">添加用户</el-button>
    </el-row>

    <div style="height: 20px"></div>

    <!-- 用户列表 -->
    <el-table :data="filteredUsers" border style="width: 100%;" stripe>
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="邮箱" prop="email" />
      <el-table-column label="手机" prop="phone" />
      <el-table-column label="角色" prop="role" />
      <el-table-column label="操作" width="200px">
        <template v-slot="scope">
          <el-button
              type="primary"
              @click="openEditDialog(scope.row)"
              class="edit-button"
              size="small"
          >编辑</el-button>
          <el-button
              type="danger"
              @click="deleteUser(scope.row.id)"
              class="delete-button"
              size="small"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-if="filteredUsers.length > 0"
        :page-size="pageSize"
        :total="filteredUsers.length"
        :current-page="currentPage"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
        class="pagination"
    />

    <!-- 添加/编辑用户弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="editUserId ? '编辑用户' : '添加用户'"
        width="500px"
        @close="resetForm"
        class="user-dialog"
    >
      <el-form :model="userForm" ref="userForm" label-width="100px">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
          <el-input v-model="userForm.username" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email" :rules="[{ required: true, message: '请输入邮箱', trigger: 'blur' }]">
          <el-input v-model="userForm.email" />
        </el-form-item>

        <el-form-item label="手机" prop="phone" :rules="[{ required: true, message: '请输入手机号码', trigger: 'blur' }]">
          <el-input v-model="userForm.phone" />
        </el-form-item>

        <el-form-item label="角色" prop="role" :rules="[{ required: true, message: '请选择角色', trigger: 'blur' }]">
          <el-select v-model="userForm.role" placeholder="选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="用户" value="user" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="submitForm" class="confirm-button">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getAllUsers, createUser, updateUser, deleteUser } from '@/api';

export default {
  name: "UserManagement",
  data() {
    return {
      users: [],
      searchQuery: '',
      pageSize: 10,
      currentPage: 1,
      dialogVisible: false,
      userForm: { username: '', email: '', phone: '', role: 'user', password: '' },
      editUserId: null,
    };
  },
  computed: {
    filteredUsers() {
      return this.users
          .filter(user => user.username.toLowerCase().includes(this.searchQuery.toLowerCase()))
          .slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize);
    }
  },
  methods: {
    async getUsers() {
      const response = await getAllUsers();
      this.users = response.data.data;
    },
    searchUser() {
      this.currentPage = 1;
    },
    openAddDialog() {
      this.dialogVisible = true;
      this.userForm = { username: '', email: '', phone: '', role: 'user', password: '' };
      this.editUserId = null;
    },
    openEditDialog(user) {
      this.dialogVisible = true;
      this.userForm = { ...user };
      this.editUserId = user.id;
    },
    async submitForm() {
      if (this.editUserId) {
        await updateUser(this.editUserId, this.userForm);
      } else {
        await createUser(this.userForm);
      }
      this.dialogVisible = false;
      this.getUsers();
    },
    async deleteUser(userId) {
      await deleteUser(userId);
      this.getUsers();
    },
    handlePageChange(page) {
      this.currentPage = page;
    },
    resetForm() {
      this.userForm = { username: '', email: '', phone: '', role: 'user' };
      this.editUserId = null;
    }
  },
  mounted() {
    this.getUsers();
  }
};
</script>

<style scoped>
.user-management {
  padding: 20px;
  overflow-y: auto;
  background-color: #f4f7fc;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 搜索框样式 */
.search-input {
  width: 250px;
  margin-right: 20px;
  border-radius: 4px;
  background-color: #ffffff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 0 0 2px #409EFF;
}

/* 添加用户按钮样式 */
.add-user-button {
  margin-left: 10px;
  height: 36px;
  border-radius: 4px;
  font-size: 14px;
  background-color: #409EFF;
  color: white;
  transition: all 0.3s ease;
}

.add-user-button:hover {
  background-color: #66b1ff;
  color: white;
}

/* 分页样式 */
.pagination {
  margin-top: 20px;
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.pagination .el-pagination__page {
  background-color: #409EFF;
  color: #fff;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.pagination .el-pagination__page:hover {
  background-color: #66b1ff;
}

.pagination .el-pagination__pager li {
  margin: 0 8px;
}

/* 表格的行和列对齐 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table-column {
  text-align: center;
}

/* 操作按钮样式 */
.edit-button, .delete-button {
  font-size: 14px;
  margin-right: 10px;
  border-radius: 4px;
  padding: 4px 8px;
  transition: all 0.3s ease;
}

.edit-button {
  background-color: #67c23a;
  color: #fff;
}

.edit-button:hover {
  background-color: #4cae2c;
}

.delete-button {
  background-color: #f56c6c;
  color: #fff;
}

.delete-button:hover {
  background-color: #e04343;
}

/* 弹窗样式 */
.user-dialog .el-dialog__header {
  background-color: #f5f5f5;
  color: #333;
  font-weight: bold;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

.dialog-footer .cancel-button {
  background-color: #f0f0f0;
  color: #333;
}

.dialog-footer .cancel-button:hover {
  background-color: #d9d9d9;
}

.dialog-footer .confirm-button {
  background-color: #409EFF;
  color: #fff;
}

.dialog-footer .confirm-button:hover {
  background-color: #66b1ff;
}
</style>
