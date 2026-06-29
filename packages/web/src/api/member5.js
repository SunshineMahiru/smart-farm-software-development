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

export async function ensureLogin() {
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
    ElMessage.warning(`后端登录未完成，页面将展示真实空状态：${error.message}`)
    return false
  }
}

export const member5Api = {
  suppliers: params => api.get('/suppliers', { params }),
  sensors: params => api.get('/iot/sensors', { params }),
  onlineStatus: params => api.get('/iot/sensors/online-status', { params }),
  farmLogs: params => api.get('/farm/logs', { params }),
  logSummary: params => api.get('/farm/logs/summary', { params }),
  yields: params => api.get('/farm/yields', { params }),
  yieldSummary: params => api.get('/farm/yields/summary', { params }),
}
