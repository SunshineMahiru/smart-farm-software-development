<template>
  <section class="page-shell stack-page">
    <Member5DataPanel title="农事日志" description="记录种植计划下的施肥、除草、巡检等农事操作，并按类型汇总。">
      <template #actions><el-button type="primary">新增日志</el-button></template>
      <div class="summary-row">
        <div v-for="item in summary" :key="item.operationType" class="summary-card">
          <span>{{ item.operationType }}</span>
          <strong>{{ item.totalNum }}</strong>
        </div>
      </div>
      <div class="toolbar">
        <el-input v-model="query.operationType" placeholder="操作类型" clearable style="width: 180px" @keyup.enter="loadData" />
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" stripe height="430" v-loading="loading">
        <el-table-column prop="operationDate" label="日期" width="130" />
        <el-table-column prop="operationType" label="操作类型" width="130" />
        <el-table-column prop="plotName" label="地块" width="150" />
        <el-table-column prop="cropName" label="作物" width="130" />
        <el-table-column prop="operatorName" label="操作员" width="130" />
        <el-table-column prop="description" label="说明" min-width="240" />
      </el-table>
    </Member5DataPanel>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureLogin, member5Api } from '../../api/member5'

const loading = ref(false)
const dateRange = ref([])
const query = reactive({ pageNum: 1, pageSize: 20, operationType: '' })
const rows = ref([])
const summary = ref([])
const fallbackRows = [
  { operationDate: '2026-06-25', operationType: '施肥', plotName: '东区1号棚', cropName: '番茄', operatorName: '丁道远', description: '追施复合肥，完成叶面观察。' },
  { operationDate: '2026-06-26', operationType: '除草', plotName: '南区2号棚', cropName: '黄瓜', operatorName: '陈晓岚', description: '人工除草并复查土壤湿度。' },
  { operationDate: '2026-06-27', operationType: '巡检', plotName: '西区3号棚', cropName: '辣椒', operatorName: '丁道远', description: '传感器状态正常，光照略高。' },
]
const fallbackSummary = [
  { operationType: '施肥', totalNum: 18 },
  { operationType: '巡检', totalNum: 32 },
  { operationType: '除草', totalNum: 11 },
]

async function loadData() {
  loading.value = true
  const params = {
    ...query,
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1],
  }
  try {
    await ensureLogin()
    const [page, stat] = await Promise.all([
      member5Api.farmLogs(params),
      member5Api.logSummary({ startDate: params.startDate, endDate: params.endDate }),
    ])
    rows.value = page.records?.length ? page.records : fallbackRows
    summary.value = stat?.length ? stat : fallbackSummary
  } catch {
    rows.value = fallbackRows
    summary.value = fallbackSummary
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.stack-page { display: grid; gap: 18px; }
.summary-row { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.summary-card { padding: 16px; border: 1px solid var(--line); border-radius: 8px; background: rgba(255,255,255,.62); }
.summary-card span { color: rgba(23,39,28,.65); }
.summary-card strong { display: block; margin-top: 8px; color: var(--leaf-dark); font-size: 28px; }
@media (max-width: 680px) { .summary-row { grid-template-columns: 1fr; } }
</style>
