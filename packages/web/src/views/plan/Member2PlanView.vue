<template>
  <section class="member2-page page-shell">
    <div class="hero panel">
      <div>
        <p class="eyebrow">Member 2 Workspace</p>
        <h1 class="page-title">实时调度与生命周期</h1>
        <p class="hero-text">
          聚合作物字典、种植计划和月度日历视图，自动根据播种日期和生长周期推算预计采收日，并在进入页面时挂接 WebSocket 通知。
        </p>
      </div>
      <div class="summary-grid">
        <article class="summary-card">
          <span>进行中计划</span>
          <strong>{{ reminders.ongoingCount }}</strong>
        </article>
        <article class="summary-card">
          <span>7天内待采收</span>
          <strong>{{ reminders.upcomingHarvestCount }}</strong>
        </article>
        <article class="summary-card">
          <span>今日启动</span>
          <strong>{{ reminders.startTodayCount }}</strong>
        </article>
        <article class="summary-card">
          <span>逾期待处理</span>
          <strong>{{ reminders.overdueCount }}</strong>
        </article>
      </div>
    </div>

    <div class="content-grid">
      <div class="panel section-card">
        <div class="section-head">
          <div>
            <p class="section-kicker">Planting Plans</p>
            <h2>种植计划排期</h2>
          </div>
          <el-button v-if="isAdmin" type="primary" @click="openPlanDialog()">新增计划</el-button>
        </div>

        <div class="toolbar">
          <el-input v-model="planQuery.keyword" placeholder="搜索地块或作物" clearable style="width: 200px" @keyup.enter="loadPlans" />
          <el-select v-model="planQuery.status" placeholder="计划状态" clearable style="width: 140px">
            <el-option v-for="item in planStatusOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="planQuery.cropId" placeholder="作物" clearable style="width: 160px">
            <el-option v-for="item in cropOptions" :key="item.cropId" :label="item.cropName" :value="item.cropId" />
          </el-select>
          <el-date-picker v-model="planDateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
          <el-button type="primary" @click="loadPlans">查询</el-button>
        </div>

        <el-table :data="planRows" v-loading="planLoading" stripe height="380" empty-text="暂无种植计划">
          <el-table-column prop="plotName" label="地块" width="120" />
          <el-table-column prop="cropName" label="作物" width="120" />
          <el-table-column prop="category" label="类别" width="110" />
          <el-table-column prop="startDate" label="播种日期" width="120" />
          <el-table-column prop="expectedHarvest" label="预计采收" width="120" />
          <el-table-column prop="plantArea" label="面积" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="planTagType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="progressPercent" label="进度" width="130">
            <template #default="{ row }">
              <el-progress :percentage="Math.max(0, Math.min(100, row.progressPercent || 0))" :stroke-width="10" :show-text="false" />
            </template>
          </el-table-column>
          <el-table-column prop="daysToHarvest" label="剩余天数" width="100" />
          <el-table-column v-if="isAdmin" label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPlanDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="removePlan(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel section-card">
        <div class="section-head">
          <div>
            <p class="section-kicker">Crop Dictionary</p>
            <h2>作物字典库</h2>
          </div>
          <el-button v-if="isAdmin" type="primary" plain @click="openCropDialog()">新增作物</el-button>
        </div>

        <div class="toolbar compact">
          <el-input v-model="cropQuery.keyword" placeholder="搜索作物" clearable style="width: 180px" @keyup.enter="loadCrops" />
          <el-button type="primary" @click="loadCrops">查询</el-button>
        </div>

        <el-table :data="cropRows" v-loading="cropLoading" stripe height="380" empty-text="暂无作物数据">
          <el-table-column prop="cropName" label="作物" width="120" />
          <el-table-column prop="category" label="类别" width="110" />
          <el-table-column prop="growthCycleDays" label="周期(天)" width="100" />
          <el-table-column prop="idealTemp" label="适温" width="90" />
          <el-table-column prop="idealHumidity" label="适湿" width="90" />
          <el-table-column v-if="isAdmin" label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openCropDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="removeCrop(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="panel calendar-card">
      <div class="section-head">
        <div>
          <p class="section-kicker">Calendar View</p>
          <h2>月度种植日历</h2>
        </div>
        <el-date-picker v-model="calendarRange" type="monthrange" value-format="YYYY-MM" range-separator="至" start-placeholder="开始月份" end-placeholder="结束月份" @change="loadCalendar" />
      </div>

      <div class="calendar-grid">
        <article v-for="group in calendarGroups" :key="group.key" class="month-column">
          <header>
            <strong>{{ group.label }}</strong>
            <span>{{ group.items.length }} 项计划</span>
          </header>
          <div class="month-list">
            <div v-for="item in group.items" :key="item.planId" class="month-item">
              <div class="item-head">
                <strong>{{ item.cropName }}</strong>
                <el-tag size="small" :type="planTagType(item.status)">{{ item.status }}</el-tag>
              </div>
              <p>{{ item.plotName }}</p>
              <small>{{ item.startDate }} → {{ item.expectedHarvest }}</small>
            </div>
          </div>
        </article>
      </div>
    </div>

    <el-dialog v-model="cropDialogVisible" :title="cropForm.cropId ? '编辑作物' : '新增作物'" width="460px">
      <el-form :model="cropForm" label-width="92px">
        <el-form-item label="作物名称"><el-input v-model="cropForm.cropName" /></el-form-item>
        <el-form-item label="作物类别"><el-input v-model="cropForm.category" /></el-form-item>
        <el-form-item label="周期天数"><el-input-number v-model="cropForm.growthCycleDays" :min="1" /></el-form-item>
        <el-form-item label="适宜温度"><el-input-number v-model="cropForm.idealTemp" :precision="1" :step="0.5" /></el-form-item>
        <el-form-item label="适宜湿度"><el-input-number v-model="cropForm.idealHumidity" :precision="1" :step="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cropDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCrop">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="planDialogVisible" :title="planForm.planId ? '编辑计划' : '新增计划'" width="560px">
      <el-form :model="planForm" label-width="96px">
        <el-form-item label="地块">
          <el-select v-model="planForm.plotId" placeholder="选择地块" style="width: 100%">
            <el-option v-for="item in plotOptions" :key="item.plotId" :label="item.plotName" :value="item.plotId" />
          </el-select>
        </el-form-item>
        <el-form-item label="作物">
          <el-select v-model="planForm.cropId" placeholder="选择作物" style="width: 100%">
            <el-option v-for="item in cropOptions" :key="item.cropId" :label="`${item.cropName} / ${item.category}`" :value="item.cropId" />
          </el-select>
        </el-form-item>
        <el-form-item label="播种日期">
          <el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="种植面积">
          <el-input-number v-model="planForm.plantArea" :min="0.1" :step="0.1" :precision="1" />
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select v-model="planForm.status" placeholder="自动推算或手动取消" style="width: 100%">
            <el-option label="自动推算" value="" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-alert v-if="harvestPreview" type="success" :closable="false" show-icon>
          预计采收日：{{ harvestPreview }}，依据当前作物生长周期自动推算。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { member2Api } from '../../api/member2'
import { closeWebSocket, initWebSocket } from '../../utils/websocket'

const currentUser = readCurrentUser()
const isAdmin = computed(() => currentUser.role === '管理员')

const reminders = reactive({
  ongoingCount: 0,
  upcomingHarvestCount: 0,
  startTodayCount: 0,
  overdueCount: 0,
})

const planLoading = ref(false)
const cropLoading = ref(false)
const planRows = ref([])
const cropRows = ref([])
const cropOptions = ref([])
const plotOptions = ref([])
const planDateRange = ref([])
const calendarRange = ref(defaultMonthRange())
const calendarItems = ref([])

const cropDialogVisible = ref(false)
const planDialogVisible = ref(false)

const cropQuery = reactive({
  pageNum: 1,
  pageSize: 50,
  keyword: '',
})

const planQuery = reactive({
  pageNum: 1,
  pageSize: 50,
  keyword: '',
  cropId: '',
  status: '',
})

const cropForm = reactive(resetCropForm())
const planForm = reactive(resetPlanForm())

const planStatusOptions = ['未开始', '进行中', '已完成', '已取消']

const harvestPreview = computed(() => {
  if (!planForm.startDate || !planForm.cropId) return ''
  const crop = cropOptions.value.find(item => item.cropId === planForm.cropId)
  if (!crop?.growthCycleDays) return ''
  return addDays(planForm.startDate, crop.growthCycleDays)
})

const calendarGroups = computed(() => {
  const map = new Map()
  calendarItems.value.forEach(item => {
    const key = item.startDate.slice(0, 7)
    if (!map.has(key)) {
      map.set(key, { key, label: formatMonthLabel(key), items: [] })
    }
    map.get(key).items.push(item)
  })
  return Array.from(map.values())
})

function readCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('user-info') || '{}')
  } catch {
    return {}
  }
}

function resetCropForm() {
  return {
    cropId: null,
    cropName: '',
    category: '',
    growthCycleDays: 90,
    idealTemp: 24,
    idealHumidity: 65,
  }
}

function resetPlanForm() {
  return {
    planId: null,
    plotId: null,
    cropId: null,
    startDate: '',
    plantArea: 10,
    status: '',
  }
}

function fillForm(target, source) {
  Object.keys(target).forEach(key => {
    target[key] = source[key] ?? (typeof target[key] === 'number' ? target[key] : '')
  })
}

function defaultMonthRange() {
  const now = new Date()
  const start = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const endDate = new Date(now.getFullYear(), now.getMonth() + 2, 1)
  const end = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}`
  return [start, end]
}

function rangeToCalendarDates(range) {
  const [startMonth, endMonth] = range || defaultMonthRange()
  return {
    startDate: `${startMonth}-01`,
    endDate: `${endMonth}-31`,
  }
}

function formatMonthLabel(value) {
  const [year, month] = value.split('-')
  return `${year}年${Number(month)}月`
}

function addDays(dateString, days) {
  const base = new Date(`${dateString}T00:00:00`)
  base.setDate(base.getDate() + Number(days))
  const year = base.getFullYear()
  const month = String(base.getMonth() + 1).padStart(2, '0')
  const day = String(base.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function planTagType(status) {
  if (status === '进行中') return 'success'
  if (status === '未开始') return 'warning'
  if (status === '已完成') return 'info'
  return 'danger'
}

async function loadBaseOptions() {
  const [cropOptionData, plotPage] = await Promise.all([
    member2Api.cropOptions(),
    member2Api.plots({ pageNum: 1, pageSize: 200 }),
  ])
  cropOptions.value = cropOptionData || []
  plotOptions.value = plotPage.records || []
}

async function loadCrops() {
  cropLoading.value = true
  try {
    const page = await member2Api.crops(cropQuery)
    cropRows.value = page.records || []
  } catch {
    cropRows.value = []
  } finally {
    cropLoading.value = false
  }
}

async function loadPlans() {
  planLoading.value = true
  try {
    const page = await member2Api.plans({
      ...planQuery,
      startDate: planDateRange.value?.[0],
      endDate: planDateRange.value?.[1],
    })
    planRows.value = page.records || []
  } catch {
    planRows.value = []
  } finally {
    planLoading.value = false
  }
}

async function loadReminders() {
  Object.assign(reminders, await member2Api.reminders())
}

async function loadCalendar() {
  const params = rangeToCalendarDates(calendarRange.value)
  calendarItems.value = await member2Api.planCalendar(params)
}

function openCropDialog(row) {
  fillForm(cropForm, resetCropForm())
  if (row) fillForm(cropForm, row)
  cropDialogVisible.value = true
}

function openPlanDialog(row) {
  fillForm(planForm, resetPlanForm())
  if (row) {
    fillForm(planForm, {
      ...row,
      status: row.status === '已取消' ? '已取消' : '',
    })
  }
  planDialogVisible.value = true
}

async function submitCrop() {
  const payload = {
    cropName: cropForm.cropName,
    category: cropForm.category,
    growthCycleDays: cropForm.growthCycleDays,
    idealTemp: cropForm.idealTemp,
    idealHumidity: cropForm.idealHumidity,
  }
  if (!payload.cropName || !payload.category) {
    ElMessage.warning('请填写完整的作物信息')
    return
  }
  if (cropForm.cropId) {
    await member2Api.updateCrop(cropForm.cropId, payload)
  } else {
    await member2Api.createCrop(payload)
  }
  cropDialogVisible.value = false
  ElMessage.success('作物信息已保存')
  await Promise.all([loadCrops(), loadBaseOptions()])
}

async function submitPlan() {
  if (!planForm.plotId || !planForm.cropId || !planForm.startDate || !planForm.plantArea) {
    ElMessage.warning('请填写完整的计划信息')
    return
  }
  const payload = {
    plotId: planForm.plotId,
    cropId: planForm.cropId,
    startDate: planForm.startDate,
    plantArea: planForm.plantArea,
    status: planForm.status,
    createdBy: Number(localStorage.getItem('userId') || currentUser.userId || 1),
  }
  if (planForm.planId) {
    await member2Api.updatePlan(planForm.planId, payload)
  } else {
    await member2Api.createPlan(payload)
  }
  planDialogVisible.value = false
  ElMessage.success('种植计划已保存')
  await Promise.all([loadPlans(), loadReminders(), loadCalendar()])
}

async function removeCrop(row) {
  await ElMessageBox.confirm(`确认删除作物“${row.cropName}”吗？`, '删除确认', { type: 'warning' })
  await member2Api.deleteCrop(row.cropId)
  ElMessage.success('作物已删除')
  await Promise.all([loadCrops(), loadBaseOptions()])
}

async function removePlan(row) {
  await ElMessageBox.confirm(`确认删除计划“${row.plotName} / ${row.cropName}”吗？`, '删除确认', { type: 'warning' })
  await member2Api.deletePlan(row.planId)
  ElMessage.success('计划已删除')
  await Promise.all([loadPlans(), loadReminders(), loadCalendar()])
}

async function initializePage() {
  try {
    await loadBaseOptions()
    await Promise.all([loadCrops(), loadPlans(), loadReminders(), loadCalendar()])
    initWebSocket()
  } catch (error) {
    ElMessage.error(error.message || '成员2页面初始化失败')
  }
}

onMounted(initializePage)
onBeforeUnmount(closeWebSocket)
</script>

<style scoped>
.member2-page { display: grid; gap: 20px; }
.hero { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(320px, .9fr); gap: 18px; padding: 28px; }
.hero-text { max-width: 640px; margin-top: 16px; color: rgba(23,39,28,.72); line-height: 1.8; }
.summary-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.summary-card { padding: 18px; border-radius: 14px; background: linear-gradient(145deg, rgba(31,111,67,.95), rgba(73,140,84,.82)); color: #fffdf2; }
.summary-card span { display: block; font-size: 13px; opacity: .82; }
.summary-card strong { display: block; margin-top: 10px; font-size: 30px; }
.content-grid { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(360px, .9fr); gap: 18px; }
.section-card, .calendar-card { padding: 24px; }
.section-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 16px; }
.section-head h2 { margin: 4px 0 0; color: var(--leaf-dark); font-size: 24px; }
.section-kicker { margin: 0; color: var(--clay); font-size: 12px; font-weight: 900; letter-spacing: .12em; text-transform: uppercase; }
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.toolbar.compact { margin-bottom: 12px; }
.calendar-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
.month-column { border: 1px solid var(--line); border-radius: 16px; padding: 16px; background: rgba(255,255,255,.58); }
.month-column header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.month-column header strong { color: var(--leaf-dark); }
.month-column header span { color: rgba(23,39,28,.58); font-size: 12px; }
.month-list { display: grid; gap: 10px; }
.month-item { padding: 12px; border-radius: 12px; background: rgba(244,248,236,.92); border: 1px solid rgba(31,111,67,.08); }
.item-head { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.month-item p { margin: 8px 0 4px; color: var(--leaf-dark); font-weight: 700; }
.month-item small { color: rgba(23,39,28,.62); }
@media (max-width: 1080px) {
  .hero, .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .summary-grid { grid-template-columns: 1fr; }
}
</style>
