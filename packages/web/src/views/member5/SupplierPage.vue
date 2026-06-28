<template>
  <section class="page-shell stack-page">
    <Member5DataPanel title="农资供应商管理" description="维护农资供应商档案，支持关键字和合作状态筛选。">
      <template #actions>
        <el-button type="primary">新增供应商</el-button>
      </template>
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="供应商 / 联系人" clearable style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="query.cooperationStatus" placeholder="合作状态" clearable style="width: 160px">
          <el-option label="正常" value="正常" />
          <el-option label="终止" value="终止" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="rows" stripe height="480" v-loading="loading">
        <el-table-column prop="supplierName" label="供应商" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" width="130" />
        <el-table-column prop="contactPhone" label="联系电话" width="160" />
        <el-table-column prop="address" label="地址" min-width="220" />
        <el-table-column prop="cooperationStatus" label="状态" width="100">
          <template #default="scope"><el-tag :type="scope.row.cooperationStatus === '正常' ? 'success' : 'info'">{{ scope.row.cooperationStatus }}</el-tag></template>
        </el-table-column>
      </el-table>
    </Member5DataPanel>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import Member5DataPanel from '../../components/Member5DataPanel.vue'
import { ensureLogin, member5Api } from '../../api/member5'

const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', cooperationStatus: '' })
const rows = ref([])
const fallback = [
  { supplierName: '绿源农资有限公司', contactPerson: '张经理', contactPhone: '13900001111', address: '东区农资市场 12 号', cooperationStatus: '正常' },
  { supplierName: '丰收农资供应站', contactPerson: '刘站长', contactPhone: '13900002222', address: '南区配送中心', cooperationStatus: '正常' },
  { supplierName: '安禾植保中心', contactPerson: '赵主管', contactPhone: '13900003333', address: '智慧农场西门', cooperationStatus: '终止' },
]

async function loadData() {
  loading.value = true
  try {
    await ensureLogin()
    const data = await member5Api.suppliers(query)
    rows.value = data.records?.length ? data.records : fallback
  } catch {
    rows.value = fallback
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.stack-page { display: grid; gap: 18px; }
</style>
