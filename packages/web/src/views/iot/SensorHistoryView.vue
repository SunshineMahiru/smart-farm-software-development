<template>
  <section class="page-shell history-page">
    <Member5DataPanel eyebrow="Member 3" title="时序数据与双 Y 轴曲线" description="按传感器查看近若干小时温湿度变化，并同步展示时序表格。">
      <div class="toolbar">
        <el-select v-model="selectedSensorId" placeholder="选择传感器" filterable style="width: 240px">
          <el-option
            v-for="sensor in sensorOptions"
            :key="sensor.sensorId"
            :label="`${sensor.sensorName} / ${sensor.sensorType}`"
            :value="sensor.sensorId"
          />
        </el-select>
        <el-select v-model="hours" style="width: 160px">
          <el-option label="近 6 小时" :value="6" />
          <el-option label="近 12 小时" :value="12" />
          <el-option label="近 24 小时" :value="24" />
          <el-option label="近 48 小时" :value="48" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadHistory">查询趋势</el-button>
      </div>

      <div class="chart-shell panel">
        <div ref="chartRef" class="chart-view"></div>
      </div>

      <el-table :data="tableRows" stripe height="320" empty-text="暂无时序数据" v-loading="loading">
        <el-table-column prop="collectTime" label="采集时间" min-width="170" />
        <el-table-column prop="temperature" label="温度(℃)" width="120" />
        <el-table-column prop="humidity" label="湿度(%)" width="120" />
        <el-table-column prop="soilMoisture" label="土壤湿度" width="120" />
        <el-table-column prop="lightIntensity" label="光照强度" min-width="140" />
      </el-table>
    </Member5DataPanel>
  </section>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureMember3Login, member3Api } from '../../api/member3'

const route = useRoute()
const chartRef = ref(null)
const chartInstance = ref(null)
const loading = ref(false)
const sensorOptions = ref([])
const selectedSensorId = ref(null)
const hours = ref(24)
const trend = ref(null)
const tableRows = ref([])

function buildChart() {
  if (!chartRef.value) return
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  const points = trend.value?.points || []
  chartInstance.value.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['温度', '湿度'] },
    grid: { left: 50, right: 50, top: 50, bottom: 60 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map(item => item.collectTime),
      axisLabel: { hideOverlap: true },
    },
    yAxis: [
      { type: 'value', name: '温度(℃)', axisLine: { show: true } },
      { type: 'value', name: '湿度(%)', axisLine: { show: true } },
    ],
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { start: 0, end: 100 },
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        symbol: 'none',
        itemStyle: { color: '#c75b39' },
        areaStyle: { color: 'rgba(199, 91, 57, 0.12)' },
        data: points.map(item => item.temperature),
      },
      {
        name: '湿度',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        symbol: 'none',
        itemStyle: { color: '#1f6f43' },
        areaStyle: { color: 'rgba(31, 111, 67, 0.12)' },
        data: points.map(item => item.humidity),
      },
    ],
  })
}

async function loadHistory() {
  if (!selectedSensorId.value) {
    ElMessage.warning('请先选择传感器')
    return
  }

  loading.value = true
  try {
    await ensureMember3Login()
    const [trendData, tableData] = await Promise.all([
      member3Api.trend({ sensorId: selectedSensorId.value, hours: hours.value }),
      member3Api.sensorData({ pageNum: 1, pageSize: 12, sensorId: selectedSensorId.value }),
    ])
    trend.value = trendData
    tableRows.value = tableData.records || []
    await nextTick()
    buildChart()
  } catch (error) {
    ElMessage.error(`趋势加载失败：${error.message}`)
  } finally {
    loading.value = false
  }
}

async function bootstrap() {
  loading.value = true
  try {
    const loggedIn = await ensureMember3Login()
    if (!loggedIn) return
    const sensorPage = await member3Api.sensors({ pageNum: 1, pageSize: 50 })
    sensorOptions.value = sensorPage.records || []
    const querySensorId = Number(route.query.sensorId)
    selectedSensorId.value = sensorOptions.value.find(item => item.sensorId === querySensorId)?.sensorId
      ?? sensorOptions.value[0]?.sensorId
      ?? null
    if (selectedSensorId.value) {
      await loadHistory()
    }
  } catch (error) {
    ElMessage.error(`传感器列表加载失败：${error.message}`)
  } finally {
    loading.value = false
  }
}

function handleResize() {
  chartInstance.value?.resize()
}

onMounted(() => {
  bootstrap()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>

<style scoped>
.history-page {
  display: grid;
  gap: 18px;
}

.chart-shell {
  margin-bottom: 18px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.chart-view {
  height: 360px;
  width: 100%;
}
</style>
