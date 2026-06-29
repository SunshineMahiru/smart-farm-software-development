<template>
  <section class="page-shell stack-page">
    <Member5DataPanel eyebrow="Member 3" title="告警记录管理" description="按状态、地块和关键字筛选告警，支持直接将未处理告警标记为已处理。">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="告警类型 / 地块名称" clearable style="width: 220px" @keyup.enter="loadAlerts" />
        <el-select v-model="query.status" placeholder="处理状态" clearable style="width: 150px">
          <el-option label="未处理" value="未处理" />
          <el-option label="已处理" value="已处理" />
        </el-select>
        <el-select v-model="query.plotId" placeholder="地块ID" clearable style="width: 150px">
          <el-option v-for="item in plotOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadAlerts">查询</el-button>
      </div>

      <el-table :data="rows" stripe height="540" empty-text="暂无告警数据" v-loading="loading">
        <el-table-column prop="alertTime" label="告警时间" min-width="170" />
        <el-table-column prop="plotName" label="地块" width="150" />
        <el-table-column prop="alertType" label="告警类型" min-width="220" />
        <el-table-column prop="alertValue" label="告警值" width="110" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === '未处理' ? 'danger' : 'success'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <RouterLink v-if="findSensorId(row.plotId)" :to="`/iot/history?sensorId=${findSensorId(row.plotId)}`">
                <el-button type="primary" link>查看曲线</el-button>
              </RouterLink>
              <el-button
                v-if="row.status === '未处理'"
                type="primary"
                link
                @click="markHandled(row)"
              >
                标记处理
              </el-button>
              <span v-else class="muted">已处理</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </Member5DataPanel>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureMember3Login, member3Api } from '../../api/member3'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const plotOptions = ref([])
const sensorRecords = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '未处理',
  plotId: '',
})

async function loadAlerts() {
  loading.value = true
  try {
    const loggedIn = await ensureMember3Login()
    if (!loggedIn) return

    const [alertPage, sensorPage] = await Promise.all([
      member3Api.alerts({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        keyword: query.keyword || undefined,
        status: query.status || undefined,
        plotId: query.plotId || undefined,
      }),
      member3Api.sensors({ pageNum: 1, pageSize: 50 }),
    ])

    rows.value = alertPage.records || []
    total.value = alertPage.total || 0
    sensorRecords.value = sensorPage.records || []
    plotOptions.value = Array.from(
      new Map((sensorPage.records || []).map(item => [item.plotId, { value: item.plotId, label: `地块 ${item.plotId}` }])).values(),
    )
  } catch (error) {
    rows.value = []
    total.value = 0
    sensorRecords.value = []
    ElMessage.error(`告警列表加载失败：${error.message}`)
  } finally {
    loading.value = false
  }
}

function findSensorId(plotId) {
  return sensorRecords.value.find(item => item.plotId === plotId)?.sensorId || ''
}

async function markHandled(row) {
  try {
    await member3Api.updateAlertStatus(row.alertId, '已处理')
    ElMessage.success('告警已标记为已处理')
    await loadAlerts()
  } catch (error) {
    ElMessage.error(`状态更新失败：${error.message}`)
  }
}

function handlePageChange(page) {
  query.pageNum = page
  loadAlerts()
}

onMounted(loadAlerts)
</script>

<style scoped>
.stack-page {
  display: grid;
  gap: 18px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.row-actions {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}
</style>
