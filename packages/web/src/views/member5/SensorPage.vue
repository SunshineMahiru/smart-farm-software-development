<template>
  <section class="page-shell stack-page">
    <Member5DataPanel title="传感器管理" description="管理传感器基础信息，并查看设备在线状态和最新采集时间。">
      <template #actions>
        <el-button type="primary">新增传感器</el-button>
      </template>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="设备名称 / 类型" clearable style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="query.status" placeholder="设备状态" clearable style="width: 150px">
          <el-option label="在线" value="在线" />
          <el-option label="离线" value="离线" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="rows" stripe height="480" v-loading="loading" empty-text="暂无传感器数据">
        <el-table-column prop="sensorName" label="传感器" min-width="190" />
        <el-table-column prop="sensorType" label="类型" width="150" />
        <el-table-column prop="plotName" label="地块" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在线' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latestCollectTime" label="最新采集时间" min-width="190" />
        <el-table-column prop="latestTemperature" label="温度" width="100" />
        <el-table-column prop="latestHumidity" label="湿度" width="100" />
      </el-table>
    </Member5DataPanel>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureLogin, member5Api } from '../../api/member5'

const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 20, status: '', keyword: '' })
const rows = ref([])

async function loadData() {
  loading.value = true
  try {
    await ensureLogin()
    const data = await member5Api.onlineStatus(query)
    rows.value = data.records || []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.stack-page { display: grid; gap: 18px; }
</style>
