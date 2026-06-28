<template>
  <section class="page-shell dashboard-page">
    <div class="dashboard-hero panel">
      <div>
        <p class="eyebrow">Member 5 Backend & CRUD</p>
        <h1 class="page-title">业务逻辑与权限系统工作台</h1>
        <p class="muted hero-desc">这里集中展示成员5负责的 RBAC、供应商、传感器、农事日志和产量统计功能。顶部导航可以跳转到每个独立业务页面。</p>
      </div>
      <div class="login-card">
        <span>接口状态</span>
        <strong>{{ apiReady ? '已连接后端' : '示例数据模式' }}</strong>
        <el-button type="primary" @click="connectBackend">重新连接</el-button>
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
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ensureLogin, member5Api } from '../../api/member5'

const apiReady = ref(false)
const metrics = ref([
  { label: '供应商档案', value: '7', hint: '农资来源维护' },
  { label: '传感器设备', value: '10', hint: '设备 CRUD 与在线状态' },
  { label: '农事日志', value: '50+', hint: '日常操作留痕' },
  { label: '产量统计', value: '汇总', hint: '计划维度分析' },
])

const tasks = [
  { index: '01', title: '供应商管理', desc: '分页、搜索、合作状态维护，服务农资供应链。', path: '/member5/suppliers' },
  { index: '02', title: '传感器管理', desc: '设备档案、状态筛选、在线状态查询。', path: '/member5/sensors' },
  { index: '03', title: '农事日志', desc: '按种植计划记录施肥、除草、巡检等操作。', path: '/member5/farm-logs' },
  { index: '04', title: '产量统计', desc: '采收记录维护与计划产量汇总。', path: '/member5/yields' },
]

async function connectBackend() {
  apiReady.value = await ensureLogin()
  if (!apiReady.value) return
  const [suppliers, sensors, logs, yields] = await Promise.allSettled([
    member5Api.suppliers({ pageNum: 1, pageSize: 1 }),
    member5Api.sensors({ pageNum: 1, pageSize: 1 }),
    member5Api.farmLogs({ pageNum: 1, pageSize: 1 }),
    member5Api.yields({ pageNum: 1, pageSize: 1 }),
  ])
  metrics.value = [
    { label: '供应商档案', value: suppliers.value?.total ?? '7', hint: '农资来源维护' },
    { label: '传感器设备', value: sensors.value?.total ?? '10', hint: '设备 CRUD 与在线状态' },
    { label: '农事日志', value: logs.value?.total ?? '50+', hint: '日常操作留痕' },
    { label: '产量统计', value: yields.value?.total ?? '汇总', hint: '计划维度分析' },
  ]
}

onMounted(connectBackend)
</script>

<style scoped>
.dashboard-page { display: grid; gap: 18px; }
.dashboard-hero { display: grid; grid-template-columns: 1fr 260px; gap: 24px; padding: clamp(24px, 5vw, 50px); }
.hero-desc { max-width: 720px; margin-top: 16px; font-size: 17px; line-height: 1.8; }
.login-card { display: grid; gap: 12px; align-content: center; padding: 22px; border-radius: 8px; background: linear-gradient(150deg, rgba(31,111,67,.12), rgba(184,219,99,.22)); border: 1px solid var(--line); }
.login-card strong { color: var(--leaf-dark); font-size: 24px; }
.metric-card small { color: rgba(23,39,28,.62); }
.task-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.task-card { padding: 22px; transition: transform .18s ease; }
.task-card:hover { transform: translateY(-5px); }
.task-card span { color: var(--clay); font-weight: 900; }
.task-card h2 { color: var(--leaf-dark); margin: 12px 0; }
.task-card p { color: rgba(23,39,28,.68); line-height: 1.7; margin: 0; }
@media (max-width: 920px) { .dashboard-hero { grid-template-columns: 1fr; } .task-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .task-grid { grid-template-columns: 1fr; } }
</style>
