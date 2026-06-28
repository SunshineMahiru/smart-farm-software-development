<template>
  <section class="page-shell stack-page">
    <Member5DataPanel title="产量统计" description="维护采收重量和质量等级，并按种植计划聚合总产量。">
      <template #actions>
        <el-button type="primary">新增产量</el-button>
      </template>

      <div class="yield-summary">
        <div class="yield-card">
          <span>产量记录数</span>
          <strong>{{ total }}</strong>
          <small>当前筛选条件下的真实记录数</small>
        </div>
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

      <el-table :data="rows" stripe height="430" v-loading="loading" empty-text="暂无产量统计数据">
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
const total = ref(0)

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
