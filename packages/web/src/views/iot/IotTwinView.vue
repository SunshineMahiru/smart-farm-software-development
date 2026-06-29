<template>
  <section class="page-shell twin-page">
    <div class="twin-hero panel">
      <div>
        <p class="eyebrow">Member 3 Digital Twin</p>
        <h1 class="page-title">2.5D 大棚热力图</h1>
        <p class="muted twin-copy">
          用 2.5D 视角展示传感器空间分布和环境热度。每个棚区卡片都来自实时接口数据，可直接下钻到单传感器历史曲线页面。
        </p>
      </div>

      <div class="hero-side">
        <div class="toolbar">
          <el-radio-group v-model="heatMode" size="small">
            <el-radio-button label="temperature">温度热度</el-radio-button>
            <el-radio-button label="humidity">湿度热度</el-radio-button>
          </el-radio-group>
          <el-button type="primary" :loading="loading" @click="loadTwin">刷新视图</el-button>
        </div>

        <div class="legend-grid">
          <span><i class="legend warm"></i>高热</span>
          <span><i class="legend mild"></i>平稳</span>
          <span><i class="legend cool"></i>偏冷/偏湿</span>
          <span><i class="legend off"></i>离线</span>
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

    <div class="twin-layout">
      <Member5DataPanel eyebrow="Member 3" title="温室空间分布" description="点击棚区卡片可查看当前状态和单传感器详情。">
        <div class="stage-shell">
          <div class="stage-plane">
            <div v-for="(lane, laneIndex) in lanes" :key="laneIndex" class="stage-lane">
              <button
                v-for="tile in lane"
                :key="tile.sensorId"
                class="plot-tile"
                :class="{
                  active: selectedTile?.sensorId === tile.sensorId,
                  offline: tile.status !== '在线',
                }"
                :style="{
                  '--tile-main': getTileColor(tile),
                  '--tile-side': getTileSideColor(tile),
                  '--tile-shadow': getTileShadow(tile),
                }"
                @click="selectedTile = tile"
              >
                <span class="tile-index">{{ tile.plotId }}</span>
                <span class="tile-sensor">{{ tile.sensorName }}</span>
                <span class="tile-value">{{ formatHeatValue(tile) }}</span>
              </button>
            </div>
          </div>
        </div>
      </Member5DataPanel>

      <Member5DataPanel eyebrow="Member 3" title="棚区详情" description="选中卡片后显示当前地块和传感器的最新环境信息。">
        <div v-if="selectedTile" class="detail-stack">
          <div class="detail-card panel">
            <span class="detail-kicker">{{ selectedTile.plotName || `地块 ${selectedTile.plotId}` }}</span>
            <h2>{{ selectedTile.sensorName }}</h2>
            <p class="muted">{{ selectedTile.sensorType }} · {{ selectedTile.status }}</p>
          </div>

          <div class="detail-metrics">
            <div class="detail-metric">
              <span>温度</span>
              <strong>{{ displayNumber(selectedTile.latestTemperature, '℃') }}</strong>
            </div>
            <div class="detail-metric">
              <span>湿度</span>
              <strong>{{ displayNumber(selectedTile.latestHumidity, '%') }}</strong>
            </div>
            <div class="detail-metric">
              <span>采集时间</span>
              <strong class="time">{{ selectedTile.latestCollectTime || '--' }}</strong>
            </div>
          </div>

          <div class="detail-actions">
            <RouterLink :to="`/iot/history?sensorId=${selectedTile.sensorId}`">
              <el-button type="primary">查看历史曲线</el-button>
            </RouterLink>
            <RouterLink to="/iot/alerts">
              <el-button>查看告警记录</el-button>
            </RouterLink>
          </div>

          <div class="linked-panels">
            <div class="linked-card panel">
              <div class="linked-head">
                <span>最近时序</span>
                <small>{{ recentData.length }} 条</small>
              </div>
              <div v-if="recentData.length" class="mini-table">
                <div v-for="item in recentData" :key="item.dataId" class="mini-row">
                  <strong>{{ item.collectTime }}</strong>
                  <span>温 {{ displayNumber(item.temperature, '℃') }}</span>
                  <span>湿 {{ displayNumber(item.humidity, '%') }}</span>
                </div>
              </div>
              <p v-else class="muted">暂无该传感器最近时序数据。</p>
            </div>

            <div class="linked-card panel">
              <div class="linked-head">
                <span>同地块最新告警</span>
                <small>{{ relatedAlerts.length }} 条</small>
              </div>
              <div v-if="relatedAlerts.length" class="mini-table">
                <div v-for="item in relatedAlerts" :key="item.alertId" class="mini-row">
                  <strong>{{ item.alertTime }}</strong>
                  <span>{{ item.alertType }}</span>
                  <el-tag size="small" :type="item.status === '未处理' ? 'danger' : 'success'">{{ item.status }}</el-tag>
                </div>
              </div>
              <p v-else class="muted">当前地块暂无告警记录。</p>
            </div>
          </div>
        </div>

        <div v-else class="empty-card panel">
          <p class="muted">暂无棚区数据，请先刷新视图。</p>
        </div>
      </Member5DataPanel>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureMember3Login, member3Api } from '../../api/member3'

const loading = ref(false)
const heatMode = ref('temperature')
const tiles = ref([])
const selectedTile = ref(null)
const recentData = ref([])
const relatedAlerts = ref([])

const metrics = computed(() => {
  const onlineTiles = tiles.value.filter(item => item.status === '在线')
  const avgTemperature = averageOf(onlineTiles, 'latestTemperature')
  const avgHumidity = averageOf(onlineTiles, 'latestHumidity')
  return [
    { label: '空间节点', value: tiles.value.length || '--', hint: '热力图覆盖棚区数' },
    { label: '在线棚区', value: onlineTiles.length || '--', hint: '活跃监测点' },
    { label: '平均温度', value: avgTemperature, hint: '实时环境热度' },
    { label: '平均湿度', value: avgHumidity, hint: '实时空气湿度' },
  ]
})

const lanes = computed(() => {
  const source = [...tiles.value]
  const result = []
  const laneSize = 3
  for (let index = 0; index < source.length; index += laneSize) {
    result.push(source.slice(index, index + laneSize))
  }
  return result
})

function averageOf(source, field) {
  const values = source.map(item => Number(item[field])).filter(value => Number.isFinite(value))
  if (!values.length) return '--'
  return `${(values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1)}`
}

function displayNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') return '--'
  return `${value}${unit}`
}

function formatHeatValue(tile) {
  if (tile.status !== '在线') return '离线'
  return heatMode.value === 'temperature'
    ? displayNumber(tile.latestTemperature, '℃')
    : displayNumber(tile.latestHumidity, '%')
}

function getTileColor(tile) {
  if (tile.status !== '在线') return 'linear-gradient(145deg, #8b948f, #6f7571)'

  const value = heatMode.value === 'temperature'
    ? Number(tile.latestTemperature)
    : Number(tile.latestHumidity)

  if (!Number.isFinite(value)) return 'linear-gradient(145deg, #9fb794, #819e78)'

  if (heatMode.value === 'temperature') {
    if (value >= 32) return 'linear-gradient(145deg, #d15e35, #f29c52)'
    if (value >= 24) return 'linear-gradient(145deg, #9cc95b, #d6e67f)'
    return 'linear-gradient(145deg, #4f8f83, #8ed2bf)'
  }

  if (value >= 75) return 'linear-gradient(145deg, #287d6e, #65c6a5)'
  if (value >= 50) return 'linear-gradient(145deg, #7fb86a, #c0e36f)'
  return 'linear-gradient(145deg, #c58c4b, #e6be73)'
}

function getTileSideColor(tile) {
  if (tile.status !== '在线') return '#59605d'
  return heatMode.value === 'temperature' ? '#844529' : '#2f6659'
}

function getTileShadow(tile) {
  if (tile.status !== '在线') return 'rgba(70, 78, 74, 0.28)'
  return heatMode.value === 'temperature'
    ? 'rgba(181, 92, 44, 0.32)'
    : 'rgba(42, 110, 94, 0.32)'
}

async function loadTwin() {
  loading.value = true
  try {
    const loggedIn = await ensureMember3Login()
    if (!loggedIn) return
    const page = await member3Api.onlineStatus({ pageNum: 1, pageSize: 50 })
    tiles.value = page.records || []
    if (!selectedTile.value || !tiles.value.find(item => item.sensorId === selectedTile.value.sensorId)) {
      selectedTile.value = tiles.value[0] || null
    }
  } catch (error) {
    tiles.value = []
    selectedTile.value = null
    recentData.value = []
    relatedAlerts.value = []
    ElMessage.error(`热力图加载失败：${error.message}`)
  } finally {
    loading.value = false
  }
}

async function loadTileDetails(tile) {
  if (!tile?.sensorId) {
    recentData.value = []
    relatedAlerts.value = []
    return
  }

  try {
    const [sensorDataPage, alertPage] = await Promise.all([
      member3Api.sensorData({ pageNum: 1, pageSize: 4, sensorId: tile.sensorId }),
      member3Api.alerts({ pageNum: 1, pageSize: 4, plotId: tile.plotId }),
    ])
    recentData.value = sensorDataPage.records || []
    relatedAlerts.value = alertPage.records || []
  } catch {
    recentData.value = []
    relatedAlerts.value = []
  }
}

onMounted(loadTwin)

watch(selectedTile, tile => {
  loadTileDetails(tile)
}, { immediate: true })
</script>

<style scoped>
.twin-page {
  display: grid;
  gap: 18px;
}

.twin-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 24px;
  padding: clamp(24px, 4vw, 40px);
}

.twin-copy {
  max-width: 760px;
  margin-top: 16px;
  line-height: 1.82;
}

.hero-side {
  display: grid;
  gap: 18px;
  align-content: center;
}

.legend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.legend-grid span {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: rgba(23, 39, 28, 0.74);
  font-size: 13px;
  font-weight: 700;
}

.legend {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 999px;
}

.legend.warm { background: linear-gradient(145deg, #d15e35, #f29c52); }
.legend.mild { background: linear-gradient(145deg, #9cc95b, #d6e67f); }
.legend.cool { background: linear-gradient(145deg, #4f8f83, #8ed2bf); }
.legend.off { background: linear-gradient(145deg, #8b948f, #6f7571); }

.twin-layout {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 18px;
}

.stage-shell {
  min-height: 520px;
  padding: 14px;
  border-radius: 8px;
  background:
    radial-gradient(circle at 50% 10%, rgba(184, 219, 99, 0.22), transparent 24rem),
    linear-gradient(180deg, rgba(255, 255, 255, 0.38), rgba(218, 231, 211, 0.62));
  overflow: auto;
}

.stage-plane {
  display: grid;
  gap: 18px;
  width: max-content;
  margin: 20px auto 10px;
  perspective: 1200px;
}

.stage-lane {
  display: grid;
  grid-auto-flow: column;
  gap: 18px;
}

.plot-tile {
  position: relative;
  display: grid;
  align-content: space-between;
  width: 172px;
  height: 128px;
  padding: 16px;
  border: none;
  border-radius: 18px;
  color: #fffdf2;
  text-align: left;
  cursor: pointer;
  background: var(--tile-main);
  transform: rotateX(58deg) rotateZ(-45deg);
  transform-style: preserve-3d;
  box-shadow: 0 28px 36px var(--tile-shadow);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.plot-tile::before,
.plot-tile::after {
  content: '';
  position: absolute;
  background: var(--tile-side);
  transform-origin: left top;
}

.plot-tile::before {
  left: 10px;
  right: 10px;
  bottom: -18px;
  height: 20px;
  border-radius: 0 0 14px 14px;
  transform: skewX(-45deg);
  filter: brightness(0.95);
}

.plot-tile::after {
  top: 10px;
  right: -18px;
  bottom: 10px;
  width: 20px;
  border-radius: 0 14px 14px 0;
  transform: skewY(-45deg);
  filter: brightness(0.82);
}

.plot-tile:hover,
.plot-tile.active {
  transform: rotateX(58deg) rotateZ(-45deg) translate3d(0, -8px, 20px);
  box-shadow: 0 34px 40px var(--tile-shadow);
}

.plot-tile.offline {
  filter: grayscale(0.12);
}

.tile-index,
.tile-sensor,
.tile-value {
  position: relative;
  z-index: 2;
  transform: rotateZ(45deg) rotateX(-58deg);
  transform-origin: left top;
}

.tile-index {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(255, 253, 242, 0.22);
  font-size: 12px;
  font-weight: 900;
}

.tile-sensor {
  margin-top: auto;
  font-size: 12px;
  line-height: 1.5;
  opacity: 0.94;
}

.tile-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 900;
}

.detail-stack {
  display: grid;
  gap: 16px;
}

.detail-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.58);
}

.detail-kicker {
  color: var(--clay);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-card h2 {
  margin: 12px 0 8px;
  color: var(--leaf-dark);
}

.detail-metrics {
  display: grid;
  gap: 12px;
}

.detail-metric {
  padding: 16px 18px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.56);
}

.detail-metric span {
  display: block;
  margin-bottom: 8px;
  color: rgba(23, 39, 28, 0.62);
}

.detail-metric strong {
  color: var(--leaf-dark);
  font-size: 24px;
}

.detail-metric strong.time {
  font-size: 16px;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.linked-panels {
  display: grid;
  gap: 14px;
}

.linked-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.56);
}

.linked-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  color: var(--leaf-dark);
  font-weight: 800;
}

.mini-table {
  display: grid;
  gap: 10px;
}

.mini-row {
  display: grid;
  gap: 4px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(23, 39, 28, 0.12);
}

.mini-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.mini-row strong {
  color: var(--leaf-dark);
  font-size: 13px;
}

.mini-row span {
  color: rgba(23, 39, 28, 0.72);
  font-size: 13px;
}

.empty-card {
  padding: 24px;
}

@media (max-width: 1080px) {
  .twin-hero,
  .twin-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stage-lane {
    gap: 14px;
  }

  .plot-tile {
    width: 140px;
    height: 108px;
    padding: 12px;
  }

  .tile-value {
    font-size: 22px;
  }
}
</style>
