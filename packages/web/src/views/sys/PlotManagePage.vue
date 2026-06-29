<template>
  <section class="page-shell stack-page">
    <Member1DataPanel title="地块台账管理" description="维护地块面积、土壤、位置与状态流转，作为种植、IoT 和农事模块的空间资产基础。">
      <template #actions>
        <el-button type="primary" :disabled="!isAdmin" @click="openCreate">新增地块</el-button>
      </template>

      <div class="plot-summary">
        <div v-for="item in summaryCards" :key="item.status" class="summary-card" :class="`status-${item.status}`">
          <span>{{ item.status }}</span>
          <strong>{{ item.totalCount || 0 }}</strong>
          <small>{{ item.totalArea || 0 }} 亩</small>
        </div>
      </div>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="地块 / 位置 / 土壤" clearable style="width: 230px" @keyup.enter="loadData" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
          <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="query.location" placeholder="位置" clearable style="width: 180px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-alert v-if="!isAdmin" class="readonly-alert" title="当前账号不是管理员，仅允许查看地块台账。" type="info" :closable="false" />

      <el-table :data="rows" stripe height="460" v-loading="loading" empty-text="暂无地块数据">
        <el-table-column prop="plotName" label="地块名称" min-width="160" />
        <el-table-column prop="area" label="面积" width="110">
          <template #default="scope">{{ scope.row.area }} 亩</template>
        </el-table-column>
        <el-table-column prop="soilType" label="土壤" width="130" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" :disabled="!isAdmin" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" :disabled="!isAdmin" @click="openStatus(scope.row)">流转</el-button>
            <el-button size="small" type="danger" :disabled="!isAdmin" @click="removePlot(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingPlotId ? '编辑地块' : '新增地块'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="地块名称" prop="plotName">
          <el-input v-model="form.plotName" placeholder="如 A区1号棚" />
        </el-form-item>
        <el-form-item label="面积" prop="area">
          <el-input-number v-model="form.area" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="土壤类型" prop="soilType">
          <el-input v-model="form.soilType" placeholder="壤土 / 黑土 / 砂壤土" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="农场东区" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlot">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="地块状态流转" width="420px">
      <div class="status-flow">
        <span>{{ statusRow?.plotName }}</span>
        <strong>{{ statusRow?.status }}</strong>
        <em>→</em>
        <el-select v-model="targetStatus" style="width: 160px">
          <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStatus">确认流转</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Member1DataPanel from '../../components/Member1DataPanel.vue'
import { member1Api } from '../../api/member1'

const statuses = ['空闲', '种植中', '休耕', '维护中']
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const editingPlotId = ref(null)
const statusRow = ref(null)
const targetStatus = ref('')
const formRef = ref()
const rows = ref([])
const summary = ref([])
const total = ref(0)
const currentUser = ref(readCurrentUser())
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', status: '', soilType: '', location: '' })
const form = reactive({ plotName: '', area: 1, soilType: '', location: '', status: '空闲' })

const isAdmin = computed(() => currentUser.value?.role === '管理员')
const summaryCards = computed(() => statuses.map(status => summary.value.find(item => item.status === status) || { status, totalCount: 0, totalArea: 0 }))

const rules = {
  plotName: [{ required: true, message: '请输入地块名称', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function readCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('user-info') || '{}')
  } catch {
    return {}
  }
}

function statusType(status) {
  return {
    空闲: 'info',
    种植中: 'success',
    休耕: 'warning',
    维护中: 'danger',
  }[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const [page, stat] = await Promise.all([
      member1Api.plots(query),
      member1Api.plotSummary(),
    ])
    rows.value = page.records || []
    total.value = page.total || 0
    summary.value = stat || []
  } catch {
    rows.value = []
    total.value = 0
    summary.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { plotName: '', area: 1, soilType: '', location: '', status: '空闲' })
}

function openCreate() {
  editingPlotId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingPlotId.value = row.plotId
  Object.assign(form, {
    plotName: row.plotName || '',
    area: Number(row.area || 1),
    soilType: row.soilType || '',
    location: row.location || '',
    status: row.status || '空闲',
  })
  dialogVisible.value = true
}

function openStatus(row) {
  statusRow.value = row
  targetStatus.value = row.status
  statusDialogVisible.value = true
}

async function savePlot() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (editingPlotId.value) {
      await member1Api.updatePlot(editingPlotId.value, payload)
      ElMessage.success('地块已更新')
    } else {
      await member1Api.createPlot(payload)
      ElMessage.success('地块已新增')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function saveStatus() {
  saving.value = true
  try {
    await member1Api.updatePlotStatus(statusRow.value.plotId, targetStatus.value)
    ElMessage.success('地块状态已更新')
    statusDialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function removePlot(row) {
  await ElMessageBox.confirm(`确认删除地块 ${row.plotName}？被业务记录引用的地块会由后端拒绝删除。`, '删除地块', { type: 'warning' })
  await member1Api.deletePlot(row.plotId)
  ElMessage.success('地块已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.stack-page { display: grid; gap: 18px; }
.plot-summary { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.summary-card { padding: 16px; border: 1px solid var(--line); border-radius: 8px; background: rgba(255,255,255,.64); }
.summary-card span, .summary-card small { color: rgba(23,39,28,.66); }
.summary-card strong { display: block; margin: 8px 0; color: var(--leaf-dark); font-size: 30px; }
.readonly-alert { margin-bottom: 14px; }
.pager { justify-content: flex-end; margin-top: 16px; }
.status-flow { display: grid; grid-template-columns: 1fr auto auto auto; align-items: center; gap: 12px; }
.status-flow strong { color: var(--leaf-dark); }
.status-flow em { color: var(--clay); font-style: normal; font-weight: 900; }
@media (max-width: 760px) { .plot-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 520px) { .plot-summary { grid-template-columns: 1fr; } }
</style>
