<template>
  <div class="welcome-container">
    <h1>🚀 智慧农场 (Smart Farm) 前端工程点亮成功！</h1>
    <p>当前项目：packages/web (Vue 3 + Vite)</p>
    
    <el-button type="primary" size="large" @click="handleClick">
      测试 Element Plus 按钮
    </el-button>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const handleClick = () => {
  ElMessage.success('🎉 恭喜！Vue 3 与 Element Plus 完美运行！')
}
</script>

<style>
body {
  margin: 0;
  background-color: #f0f2f5;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.welcome-container {
  margin: 100px auto;
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  width: 600px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
h1 {
  color: #409EFF;
}
</style>