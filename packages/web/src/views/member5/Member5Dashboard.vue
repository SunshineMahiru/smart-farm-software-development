<template>
  <section class="page-shell dashboard-page">
    <div class="dashboard-hero panel">
      <div>
        <p class="eyebrow">Member 5 Backend & CRUD</p>
        <h1 class="page-title">成员5业务总览与权限工作台</h1>
        <p class="muted hero-desc">
          这里集中展示成员5负责的权限、供应商、传感器、农事日志和产量统计功能。页面不会自动轮询后端，只有点击“重新连接”时才检测一次接口状态。
        </p>
      </div>
      <div class="login-card">
        <span>接口状态</span>
        <strong>{{ statusText }}</strong>
        <el-button type="primary" :loading="checking" @click="connectBackend">重新连接</el-button>
      </div>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </div>
    </div>

    <div class="task-grid">
      <RouterLink v-for="item in tasks" :key="item.path" :to="item.path" class="task-card panel">
        <span>{{ item.index }}</span>
        <h2>{{ item.title }}</h2>
        <p>{{ item.desc }}</p>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ensureLogin, member5Api } from '../../api/member5'

const checking = ref(false)
const apiReady = ref(null)
const metrics = ref([
  { label: '供应商档案', value: 0, hint: '农资来源维护' },
  { label: '传感器设备', value: 0, hint: '设备 CRUD 与在线状态' },
  { label: '农事日志', value: 0, hint: '日常操作留痕' },
  { label: '产量统计', value: 0, hint: '计划维度分析' },
])

const statusText = computed(() => {
  if (apiReady.value === true) return '已连接后端'
  if (apiReady.value === false) return '未连接后端'
  return '未检测'
})

const tasks = [
  { index: '01', title: '供应商管理', desc: '分页、搜索与合作状态维护，服务农资供应链。', path: '/member5/suppliers' },
  { index: '02', title: '传感器管理', desc: '设备档案、状态筛选与在线状态查询。', path: '/member5/sensors' },
  { index: '03', title: '农事日志', desc: '按种植计划记录施肥、巡检、除草等操作。', path: '/member5/farm-logs' },
  { index: '04', title: '产量统计', desc: '采收记录维护与计划产量汇总分析。', path: '/member5/yields' },
]

function totalOf(result) {
  return result.status === 'fulfilled' ? (result.value?.total || 0) : 0
}

function resetMetrics() {
  metrics.value = metrics.value.map(item => ({ ...item, value: 0 }))
}

async function connectBackend() {
  checking.value = true
  try {
    const loggedIn = await ensureLogin()
    if (!loggedIn) {
      apiReady.value = false
      resetMetrics()
      return
    }

    const [suppliers, sensors, logs, yields] = await Promise.allSettled([
      member5Api.suppliers({ pageNum: 1, pageSize: 1 }),
      member5Api.sensors({ pageNum: 1, pageSize: 1 }),
      member5Api.farmLogs({ pageNum: 1, pageSize: 1 }),
      member5Api.yields({ pageNum: 1, pageSize: 1 }),
    ])

    apiReady.value = [suppliers, sensors, logs, yields].some(result => result.status === 'fulfilled')
    metrics.value = [
      { label: '供应商档案', value: totalOf(suppliers), hint: '农资来源维护' },
      { label: '传感器设备', value: totalOf(sensors), hint: '设备 CRUD 与在线状态' },
      { label: '农事日志', value: totalOf(logs), hint: '日常操作留痕' },
      { label: '产量统计', value: totalOf(yields), hint: '计划维度分析' },
    ]
  } catch {
    apiReady.value = false
    resetMetrics()
  } finally {
    checking.value = false
  }
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.dashboard-hero {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 24px;
  padding: clamp(24px, 5vw, 50px);
}

.hero-desc {
  max-width: 720px;
  margin-top: 16px;
  font-size: 17px;
  line-height: 1.8;
}

.login-card {
  display: grid;
  gap: 12px;
  align-content: center;
  padding: 22px;
  border-radius: 8px;
  background: linear-gradient(150deg, rgba(31, 111, 67, 0.12), rgba(184, 219, 99, 0.22));
  border: 1px solid var(--line);
}

.login-card strong {
  color: var(--leaf-dark);
  font-size: 24px;
}

.metric-card small {
  color: rgba(23, 39, 28, 0.62);
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.task-card {
  padding: 22px;
  transition: transform 0.18s ease;
}

.task-card:hover {
  transform: translateY(-5px);
}

.task-card span {
  color: var(--clay);
  font-weight: 900;
}

.task-card h2 {
  color: var(--leaf-dark);
  margin: 12px 0;
}

.task-card p {
  color: rgba(23, 39, 28, 0.68);
  line-height: 1.7;
  margin: 0;
}

@media (max-width: 920px) {
  .dashboard-hero {
    grid-template-columns: 1fr;
  }

  .task-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
