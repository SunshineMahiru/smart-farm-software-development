import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入 Element Plus 的样式表

const app = createApp(App)

app.use(ElementPlus)
app.mount('#app')