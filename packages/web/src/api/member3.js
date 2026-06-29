import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('sa-token')
  if (token) config.headers.Authorization = token
  return config
})

api.interceptors.response.use(
  response => {
    const body = response.data
    if (body?.code && body.code !== 200) {
      return Promise.reject(new Error(body.msg || 'Request failed'))
    }
    return body?.data ?? body
  },
  error => Promise.reject(new Error(error?.response?.data?.msg || error.message || 'Network error')),
)

export async function ensureMember3Login() {
  const token = localStorage.getItem('sa-token')
  if (token) {
    try {
      await api.get('/sys/user/me')
      return true
    } catch {
      localStorage.removeItem('sa-token')
      localStorage.removeItem('userId')
    }
  }

  try {
    const data = await api.post('/sys/user/login', {
      username: 'admin01',
      password: '123456',
    })
    localStorage.setItem('sa-token', data.token)
    localStorage.setItem('userId', data.userId)
    return true
  } catch (error) {
    ElMessage.warning(`成员3页面自动登录失败：${error.message}`)
    return false
  }
}

export const member3Api = {
  overview: () => api.get('/iot/overview'),
  latestAlerts: params => api.get('/iot/alerts/latest', { params }),
  alerts: params => api.get('/iot/alerts', { params }),
  updateAlertStatus: (alertId, status) => api.patch(`/iot/alerts/${alertId}/status`, { status }),
  sensors: params => api.get('/iot/sensors', { params }),
  onlineStatus: params => api.get('/iot/sensors/online-status', { params }),
  sensorData: params => api.get('/iot/sensor-data', { params }),
  trend: params => api.get('/iot/history/trend', { params }),
  latestReport: () => api.get('/iot/reports/latest'),
  generateReport: () => api.post('/iot/reports/generate'),
}
