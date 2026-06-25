import { ElNotification } from 'element-plus'

let socket = null
let lockReconnect = false // 避免重复连接
let reconnectTimer = null

export function initWebSocket() {
  const userId = localStorage.getItem('userId')
  if (!userId) return

  // 替换为你后端的实际地址
  const wsUrl = `ws://127.0.0.1:8080/ws/${userId}`
  
  try {
    socket = new WebSocket(wsUrl)
    initEventHandle()
  } catch (e) {
    reconnect()
  }
}

function initEventHandle() {
  socket.onclose = () => {
    console.log('【WebSocket】连接已关闭')
    reconnect()
  }
  
  socket.onerror = () => {
    console.log('【WebSocket】发生异常')
    reconnect()
  }
  
  socket.onopen = () => {
    console.log('【WebSocket】连接成功')
    // 心跳检测可以在这里启动
  }
  
  socket.onmessage = (event) => {
    // 重点：收到后端推送的 AI 诊断或告警，直接在全局弹出优雅通知！
    const msg = event.data
    ElNotification({
      title: '智慧农场 AI 智能中枢',
      message: msg,
      type: 'warning',
      duration: 8000,
      position: 'top-right'
    })
  }
}

function reconnect() {
  if (lockReconnect) return
  lockReconnect = true
  
  reconnectTimer && clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => {
    initWebSocket()
    lockReconnect = false
  }, 5000) // 5秒后重连
}

export function closeWebSocket() {
  if (socket) {
    socket.close()
  }
}