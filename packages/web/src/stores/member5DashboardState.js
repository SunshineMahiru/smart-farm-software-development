import { ref } from 'vue'

function createDefaultMetrics() {
  return [
    { label: '供应商档案', value: 0, hint: '农资来源维护' },
    { label: '传感器设备', value: 0, hint: '设备 CRUD 与在线状态' },
    { label: '农事日志', value: 0, hint: '日常操作留痕' },
    { label: '产量统计', value: 0, hint: '计划维度分析' },
  ]
}

export const member5Checking = ref(false)
export const member5ApiReady = ref(null)
export const member5Metrics = ref(createDefaultMetrics())

export function resetMember5Metrics() {
  member5Metrics.value = createDefaultMetrics()
}

export function setMember5Metrics(metrics) {
  member5Metrics.value = metrics
}
