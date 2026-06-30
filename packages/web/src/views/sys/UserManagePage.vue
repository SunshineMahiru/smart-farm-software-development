<template>
  <section class="page-shell stack-page">
    <div class="sys-page-hero panel">
      <div>
        <p class="eyebrow">RBAC Console</p>
        <h1 class="page-title">用户权限治理</h1>
        <p class="muted">以账号、角色和业务引用保护为核心，确保系统基建具备清晰的操作边界。</p>
      </div>
      <RouterLink to="/sys" class="back-dashboard">返回系统工作台</RouterLink>
    </div>

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
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <div class="role-tabs">
        <button v-for="item in roleTabs" :key="item.value" type="button" :class="{ active: query.role === item.value }" @click="setRole(item.value)">
          <span>{{ item.label }}</span>
          <strong>{{ item.count }}</strong>
        </button>
      </div>

      <el-alert v-if="!isAdmin" class="readonly-alert" title="当前账号不是管理员，仅允许查看用户列表。" type="info" :closable="false" />

      <el-table :data="rows" stripe height="460" v-loading="loading" empty-text="暂无用户数据" class="govern-table">
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
import { RouterLink } from 'vue-router'
import Member1DataPanel from '../../components/Member1DataPanel.vue'
import { authApi, member1Api } from '../../api/member1'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingUserId = ref(null)
const formRef = ref()
const rows = ref([])
const allUsers = ref([])
const total = ref(0)
const currentUser = ref(readCurrentUser())
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', role: '' })
const form = reactive({ username: '', password: '123456', realName: '', role: '农技员', phone: '' })

const currentRole = computed(() => currentUser.value?.role || '')
const isAdmin = computed(() => currentRole.value === '管理员')
const adminCount = computed(() => allUsers.value.filter(item => item.role === '管理员').length)
const techCount = computed(() => allUsers.value.filter(item => item.role === '农技员').length)
const roleTabs = computed(() => [
  { label: '全部账号', value: '', count: total.value },
  { label: '管理员', value: '管理员', count: adminCount.value },
  { label: '农技员', value: '农技员', count: techCount.value },
])

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
    const [data, allData] = await Promise.all([
      member1Api.users(query),
      member1Api.users({ pageNum: 1, pageSize: 200 }),
    ])
    rows.value = data.records || []
    allUsers.value = allData.records || []
    total.value = data.total || 0
  } catch {
    rows.value = []
    allUsers.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function setRole(role) {
  query.role = role
  query.pageNum = 1
  loadData()
}

function resetQuery() {
  Object.assign(query, { pageNum: 1, pageSize: query.pageSize, keyword: '', role: '' })
  loadData()
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
.sys-page-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 26px;
  background:
    radial-gradient(circle at 18% 18%, rgba(184, 219, 99, 0.34), transparent 18rem),
    rgba(255, 255, 250, 0.82);
}

.sys-page-hero .muted {
  max-width: 680px;
  margin: 14px 0 0;
  line-height: 1.7;
}

.back-dashboard {
  padding: 12px 16px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.66);
  color: var(--leaf-dark);
  font-weight: 900;
  white-space: nowrap;
}

.user-metrics { margin-bottom: 16px; }
.role-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 0 0 16px;
}

.role-tabs button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.52);
  color: var(--leaf-dark);
  cursor: pointer;
}

.role-tabs button.active {
  border-color: rgba(31, 111, 67, 0.44);
  background: rgba(31, 111, 67, 0.1);
}

.role-tabs span {
  font-weight: 900;
}

.role-tabs strong {
  font-size: 22px;
}

.readonly-alert { margin-bottom: 14px; }
.pager { justify-content: flex-end; margin-top: 16px; }
@media (max-width: 720px) {
  .sys-page-hero { align-items: flex-start; flex-direction: column; }
  .role-tabs { grid-template-columns: 1fr; }
}
</style>
