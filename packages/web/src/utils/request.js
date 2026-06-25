import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 自动为每次请求挂载 Sa-Token
    const token = localStorage.getItem('sa-token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 统一处理后端 Result 格式
    if (res.code === 200) {
      return res.data
    } 
    // 处理 Sa-Token 拦截器抛出的 401 未登录异常
    else if (res.code === 401) {
      ElMessage.error('登录状态已过期，请重新登录')
      localStorage.removeItem('sa-token')
      router.push('/login')
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      ElMessage.error(res.msg || '系统异常')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    ElMessage.error('网络请求异常，请稍后再试')
    return Promise.reject(error)
  }
)

export default service