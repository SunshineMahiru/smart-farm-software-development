<template>
  <section class="page-shell stack-page">
    <Member1DataPanel title="用户权限管理" description="维护平台账号、角色和联系方式。管理员可新增、编辑、删除和重置密码。">
      <template #actions>
        <el-button type="primary" :disabled="!isAdmin" @click="openCreate">新增用户</el-button>
      </template>

      <div class="metric-grid user-metrics">
        <div class="metric-card">
          <span>用户总数</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="metric-card">
          <span>管理员</span>
          <strong>{{ adminCount }}</strong>
        </div>
        <div class="metric-card">
          <span>农技员</span>
          <strong>{{ techCount }}</strong>
        </div>
        <div class="metric-card">
          <span>当前角色</span>
          <strong>{{ currentRole || '-' }}</strong>
        </div>
      </div>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="账号 / 姓名 / 电话" clearable style="width: 230px" @keyup.enter="loadData" />
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 150px">
          <el-option label="管理员" value="管理员" />
          <el-option label="农技员" value="农技员" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-alert v-if="!isAdmin" class="readonly-alert" title="当前账号不是管理员，仅允许查看用户列表。" type="info" :closable="false" />

      <el-table :data="rows" stripe height="460" v-loading="loading" empty-text="暂无用户数据">
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="130" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === '管理员' ? 'success' : 'info'">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="160" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" :disabled="!isAdmin" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" :disabled="!isAdmin" @click="resetPassword(scope.row)">重置密码</el-button>
            <el-button size="small" type="danger" :disabled="!isAdmin" @click="removeUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </Member1DataPanel>

    <el-dialog v-model="dialogVisible" :title="editingUserId ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="至少4位" />
        </el-form-item>
        <el-form-item v-if="!editingUserId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="管理员" />
            <el-option label="农技员" value="农技员" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号，可为空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Member1DataPanel from '../../components/Member1DataPanel.vue'
import { authApi, member1Api } from '../../api/member1'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingUserId = ref(null)
const formRef = ref()
const rows = ref([])
const total = ref(0)
const currentUser = ref(readCurrentUser())
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', role: '' })
const form = reactive({ username: '', password: '123456', realName: '', role: '农技员', phone: '' })

const currentRole = computed(() => currentUser.value?.role || '')
const isAdmin = computed(() => currentRole.value === '管理员')
const adminCount = computed(() => rows.value.filter(item => item.role === '管理员').length)
const techCount = computed(() => rows.value.filter(item => item.role === '农技员').length)

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 50, message: '账号长度需为4到50位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度需为6到100位', trigger: 'blur' },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

function readCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('user-info') || '{}')
  } catch {
    return {}
  }
}

async function loadCurrentUser() {
  const user = await authApi.me()
  currentUser.value = user
  localStorage.setItem('user-info', JSON.stringify(user))
}

async function loadData() {
  loading.value = true
  try {
    if (!currentUser.value?.role) await loadCurrentUser()
    const data = await member1Api.users(query)
    rows.value = data.records || []
    total.value = data.total || 0
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { username: '', password: '123456', realName: '', role: '农技员', phone: '' })
}

function openCreate() {
  editingUserId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingUserId.value = row.userId
  Object.assign(form, {
    username: row.username || '',
    password: '',
    realName: row.realName || '',
    role: row.role || '农技员',
    phone: row.phone || '',
  })
  dialogVisible.value = true
}

async function saveUser() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      username: form.username,
      realName: form.realName,
      role: form.role,
      phone: form.phone,
    }
    if (editingUserId.value) {
      await member1Api.updateUser(editingUserId.value, payload)
      ElMessage.success('用户已更新')
    } else {
      await member1Api.createUser({ ...payload, password: form.password })
      ElMessage.success('用户已新增')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function resetPassword(row) {
  const result = await ElMessageBox.prompt(`为 ${row.username} 设置新密码`, '重置密码', {
    inputType: 'password',
    inputValue: '123456',
    inputPattern: /^.{6,100}$/,
    inputErrorMessage: '密码长度需为6到100位',
  })
  await member1Api.resetPassword(row.userId, { password: result.value })
  ElMessage.success('密码已重置')
}

async function removeUser(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？该操作会受后端业务引用保护。`, '删除用户', { type: 'warning' })
  await member1Api.deleteUser(row.userId)
  ElMessage.success('用户已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.stack-page { display: grid; gap: 18px; }
.user-metrics { margin-bottom: 16px; }
.readonly-alert { margin-bottom: 14px; }
.pager { justify-content: flex-end; margin-top: 16px; }
</style>
