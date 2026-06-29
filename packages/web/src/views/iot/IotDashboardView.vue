<template>
  <section class="page-shell iot-page">
    <div class="iot-hero panel">
      <div>
        <p class="eyebrow">Member 3 IoT</p>
        <h1 class="page-title">智能感知与农情预警中枢</h1>
        <p class="muted hero-copy">
          聚合传感器状态、微气候日报与实时告警，作为成员3答辩展示的总入口。页面接入后端真实数据，并监听 WebSocket 推送。
        </p>
      </div>
      <div class="hero-actions">
        <div class="status-card">
          <span>接口状态</span>
          <strong>{{ apiReady ? '已连接' : '未连接' }}</strong>
          <small>WebSocket：{{ socketReady ? '已连接' : '未连接' }}</small>
        </div>
        <div class="button-row">
          <el-button :loading="loading" @click="loadPage">刷新数据</el-button>
          <el-button type="primary" :loading="reportLoading" @click="generateReport">生成日报</el-button>
        </div>
      </div>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </div>
    </div>

    <div class="iot-grid">
      <Member5DataPanel eyebrow="Member 3" title="最新告警" description="显示最近几条设备告警，便于快速定位异常地块。">
        <template #actions>
          <RouterLink to="/iot/alerts">
            <el-button type="primary">进入告警台</el-button>
          </RouterLink>
        </template>

        <el-table :data="alerts" stripe height="320" empty-text="暂无告警数据" v-loading="loading">
          <el-table-column prop="alertTime" label="时间" min-width="170" />
          <el-table-column prop="plotName" label="地块" width="150" />
          <el-table-column prop="alertType" label="告警类型" min-width="200" />
          <el-table-column prop="alertValue" label="数值" width="110" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === '未处理' ? 'danger' : 'success'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </Member5DataPanel>

      <Member5DataPanel eyebrow="Member 3" title="农情日报" description="展示后端生成的最新 24 小时环境聚合结论。">
        <template #actions>
          <div class="panel-actions">
            <RouterLink to="/iot/twin">
              <el-button type="primary" plain>进入 2.5D 热力图</el-button>
            </RouterLink>
            <RouterLink to="/iot/history">
              <el-button>查看历史曲线</el-button>
            </RouterLink>
          </div>
        </template>

        <div class="report-card panel">
          <div class="report-stats">
            <span>统计窗口</span>
            <strong>{{ reportWindow }}</strong>
            <small>生成时间：{{ report?.generatedAt || '--' }}</small>
          </div>
          <p class="report-content">{{ report?.reportContent || '暂无日报，请点击“生成日报”创建。' }}</p>
        </div>
      </Member5DataPanel>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureMember3Login, member3Api } from '../../api/member3'
import { closeWebSocket, initWebSocket } from '../../utils/websocket'

const loading = ref(false)
const reportLoading = ref(false)
const apiReady = ref(false)
const socketReady = ref(false)
const overview = ref(null)
const alerts = ref([])
const report = ref(null)

const metrics = computed(() => [
  { label: '传感器总数', value: overview.value?.totalSensors ?? '--', hint: 'IoT 资产规模' },
  { label: '在线设备', value: overview.value?.onlineSensors ?? '--', hint: '实时在线状态' },
  { label: '今日采集', value: overview.value?.todayDataCount ?? '--', hint: '今日新增数据量' },
  { label: '未处理告警', value: overview.value?.pendingAlerts ?? '--', hint: '待响应异常' },
])

const reportWindow = computed(() => {
  if (!report.value?.dataStartTime || !report.value?.dataEndTime) return '--'
  return `${report.value.dataStartTime} ~ ${report.value.dataEndTime}`
})

async function loadPage() {
  loading.value = true
  try {
    const loggedIn = await ensureMember3Login()
    if (!loggedIn) {
      apiReady.value = false
      return
    }

    const [overviewData, latestAlerts, latestReport] = await Promise.all([
      member3Api.overview(),
      member3Api.latestAlerts({ limit: 6 }),
      member3Api.latestReport().catch(() => null),
    ])

    overview.value = overviewData
    alerts.value = latestAlerts || []
    report.value = latestReport
    apiReady.value = true

    if (!socketReady.value) {
      initWebSocket()
      socketReady.value = true
    }
  } catch (error) {
    apiReady.value = false
    socketReady.value = false
    ElMessage.error(`成员3总览加载失败：${error.message}`)
  } finally {
    loading.value = false
  }
}

async function generateReport() {
  reportLoading.value = true
  try {
    await ensureMember3Login()
    report.value = await member3Api.generateReport()
    ElMessage.success('日报已生成')
    await loadPage()
  } catch (error) {
    ElMessage.error(`生成日报失败：${error.message}`)
  } finally {
    reportLoading.value = false
  }
}

onMounted(loadPage)

onBeforeUnmount(() => {
  closeWebSocket()
  socketReady.value = false
})
</script>

<style scoped>
.iot-page {
  display: grid;
  gap: 18px;
}

.iot-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  padding: clamp(24px, 4vw, 40px);
}

.hero-copy {
  max-width: 760px;
  margin-top: 16px;
  line-height: 1.85;
  font-size: 16px;
}

.hero-actions {
  display: grid;
  gap: 14px;
  align-content: center;
}

.status-card {
  display: grid;
  gap: 8px;
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: linear-gradient(150deg, rgba(31, 111, 67, 0.1), rgba(184, 219, 99, 0.2));
}

.status-card strong {
  color: var(--leaf-dark);
  font-size: 24px;
}

.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.iot-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.report-card {
  padding: 18px;
  background: rgba(255, 255, 255, 0.58);
  border-radius: 8px;
}

.panel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.report-stats {
  display: grid;
  gap: 6px;
  margin-bottom: 14px;
}

.report-stats strong {
  color: var(--leaf-dark);
  font-size: 18px;
}

.report-content {
  margin: 0;
  color: rgba(23, 39, 28, 0.74);
  line-height: 1.85;
}

@media (max-width: 980px) {
  .iot-hero,
  .iot-grid {
    grid-template-columns: 1fr;
  }
}
</style>
