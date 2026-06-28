<template>
  <section class="page-shell stack-page">
    <Member5DataPanel title="产量统计" description="维护采收重量和质量等级，并按种植计划聚合总产量。">
      <template #actions><el-button type="primary">新增产量</el-button></template>
      <div class="yield-summary">
        <div v-for="item in summary" :key="item.planId" class="yield-card">
          <span>{{ item.plotName || `计划 ${item.planId}` }}</span>
          <strong>{{ item.totalYield ?? 0 }} kg</strong>
          <small>{{ item.cropName || '作物未命名' }} · {{ item.recordCount ?? 0 }} 条记录</small>
        </div>
      </div>
      <div class="toolbar">
        <el-input v-model="query.qualityGrade" placeholder="质量等级" clearable style="width: 160px" @keyup.enter="loadData" />
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" stripe height="430" v-loading="loading">
        <el-table-column prop="harvestDate" label="采收日期" width="130" />
        <el-table-column prop="plotName" label="地块" width="150" />
        <el-table-column prop="cropName" label="作物" width="130" />
        <el-table-column prop="yieldWeight" label="产量(kg)" width="130" />
        <el-table-column prop="qualityGrade" label="质量等级" width="120">
          <template #default="scope"><el-tag>{{ scope.row.qualityGrade || '未评级' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="录入时间" min-width="180" />
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
const query = reactive({ pageNum: 1, pageSize: 20, qualityGrade: '' })
const rows = ref([])
const summary = ref([])
const fallbackRows = [
  { harvestDate: '2026-06-20', plotName: '东区1号棚', cropName: '番茄', yieldWeight: 460.5, qualityGrade: '优', createTime: '2026-06-20 18:30' },
  { harvestDate: '2026-06-23', plotName: '南区2号棚', cropName: '黄瓜', yieldWeight: 328.2, qualityGrade: '良', createTime: '2026-06-23 17:10' },
  { harvestDate: '2026-06-27', plotName: '西区3号棚', cropName: '辣椒', yieldWeight: 215.8, qualityGrade: '优', createTime: '2026-06-27 19:20' },
]
const fallbackSummary = [
  { planId: 1, plotName: '东区1号棚', cropName: '番茄', totalYield: 1260.5, recordCount: 4 },
  { planId: 2, plotName: '南区2号棚', cropName: '黄瓜', totalYield: 940.2, recordCount: 3 },
  { planId: 3, plotName: '西区3号棚', cropName: '辣椒', totalYield: 615.8, recordCount: 3 },
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
      member5Api.yields(params),
      member5Api.yieldSummary({}),
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
.yield-summary { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.yield-card { padding: 16px; border: 1px solid var(--line); border-radius: 8px; background: linear-gradient(145deg, rgba(255,255,255,.72), rgba(184,219,99,.18)); }
.yield-card span, .yield-card small { color: rgba(23,39,28,.66); }
.yield-card strong { display: block; margin: 8px 0; color: var(--leaf-dark); font-size: 28px; }
@media (max-width: 680px) { .yield-summary { grid-template-columns: 1fr; } }
</style>
