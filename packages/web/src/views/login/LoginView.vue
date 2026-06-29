<template>
  <section class="login-page">
    <div class="login-shell">
      <div class="login-visual panel">
        <p class="eyebrow">Member 1 Security Gateway</p>
        <h1>智慧农场<br />统一入口</h1>
        <p>登录后进入系统主骨架，按角色动态展示用户权限管理、地块台账与各成员业务模块。</p>

        <div class="field-orbit">
          <span v-for="item in 20" :key="item" :style="{ '--delay': `${item * 34}ms` }" />
        </div>
      </div>

      <div class="login-card panel">
        <div class="login-head">
          <span class="brand-mark">SF</span>
          <div>
            <h2>账号登录</h2>
            <p class="muted">默认演示账号：admin01 / 123456</p>
          </div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入账号" @keyup.enter="submit" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password size="large" placeholder="请输入密码" @keyup.enter="submit" />
          </el-form-item>
          <el-button class="login-button" type="primary" size="large" :loading="loading" @click="submit">进入控制台</el-button>
        </el-form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/member1'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin01',
  password: '123456',
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const login = await authApi.login(form)
    localStorage.setItem('sa-token', login.token)
    localStorage.setItem('userId', login.userId)
    localStorage.setItem('user-info', JSON.stringify({
      userId: login.userId,
      username: login.username,
      realName: login.realName,
      role: login.role,
    }))
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 36px 18px;
}

.login-shell {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) 420px;
  gap: 18px;
}

.login-visual {
  min-height: 620px;
  padding: clamp(30px, 6vw, 64px);
  overflow: hidden;
  background:
    radial-gradient(circle at 24% 18%, rgba(184, 219, 99, 0.38), transparent 18rem),
    linear-gradient(145deg, rgba(18, 61, 43, 0.95), rgba(31, 111, 67, 0.88));
  color: #fffdf2;
}

.login-visual .eyebrow {
  color: var(--lime);
}

.login-visual h1 {
  margin: 0;
  font-size: clamp(48px, 8vw, 88px);
  line-height: 0.94;
  letter-spacing: -0.04em;
}

.login-visual p {
  max-width: 520px;
  color: rgba(255, 253, 242, 0.78);
  font-size: 18px;
  line-height: 1.8;
}

.field-orbit {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-top: 42px;
  transform: rotateX(56deg) rotateZ(-12deg);
  transform-origin: center;
}

.field-orbit span {
  min-height: 74px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(184, 219, 99, 0.92), rgba(255, 253, 242, 0.18));
  box-shadow: inset 0 0 0 1px rgba(255,255,255,.24), 0 18px 42px rgba(0,0,0,.18);
  animation: tile-in .6s ease both;
  animation-delay: var(--delay);
}

.login-card {
  align-self: center;
  padding: 30px;
}

.login-head {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 26px;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 10px;
  background: var(--leaf-dark);
  color: var(--lime);
  font-weight: 900;
}

.login-head h2 {
  margin: 0 0 4px;
  color: var(--leaf-dark);
  font-size: 28px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

@keyframes tile-in {
  from { opacity: 0; transform: translateY(24px) scale(.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@media (max-width: 900px) {
  .login-shell { grid-template-columns: 1fr; }
  .login-visual { min-height: 420px; }
}
</style>
