<template>
  <div class="app-frame">
    <header class="topbar page-shell">
      <RouterLink to="/" class="brand">
        <span class="brand-mark">SF</span>
        <span>
          <b>智慧农场</b>
          <small>Smart Farm Console</small>
        </span>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/" class="nav-link">首页</RouterLink>

        <div class="member-group" :class="{ active: isMember1Route }">
          <RouterLink to="/sys/users" class="nav-link member-link">
            <span>成员1</span>
            <i class="arrow">▼</i>
          </RouterLink>

          <div class="member-subnav">
            <RouterLink v-for="item in member1Menus" :key="item.path" :to="item.path" class="subnav-link">{{ item.title }}</RouterLink>
          </div>
        </div>

        <div class="member-group" :class="{ active: isMember3Route }">
          <RouterLink to="/iot" class="nav-link member-link">
            <span>成员3</span>
            <i class="arrow">▼</i>
          </RouterLink>

          <div class="member-subnav">
            <RouterLink to="/iot" class="subnav-link">IoT 总览</RouterLink>
            <RouterLink to="/iot/twin" class="subnav-link">2.5D 热力图</RouterLink>
            <RouterLink to="/iot/alerts" class="subnav-link">告警管理</RouterLink>
            <RouterLink to="/iot/history" class="subnav-link">历史曲线</RouterLink>
          </div>
        </div>

        <div class="member-group" :class="{ active: isMember5Route }">
          <RouterLink to="/member5" class="nav-link member-link">
            <span>成员5</span>
            <i class="arrow">▼</i>
          </RouterLink>

          <div class="member-subnav">
            <RouterLink to="/member5/suppliers" class="subnav-link">供应商</RouterLink>
            <RouterLink to="/member5/sensors" class="subnav-link">传感器</RouterLink>
            <RouterLink to="/member5/farm-logs" class="subnav-link">农事日志</RouterLink>
            <RouterLink to="/member5/yields" class="subnav-link">产量统计</RouterLink>
          </div>
        </div>
      </nav>

      <div class="user-box">
        <span>{{ userLabel }}</span>
        <small>{{ currentUser.role || '未识别角色' }}</small>
        <el-button size="small" @click="logout">退出</el-button>
      </div>
    </header>

    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { authApi } from '../api/member1'

const route = useRoute()
const router = useRouter()
const currentUser = ref(readCachedUser())

const member1Menus = computed(() => {
  const menus = [
    { title: '地块台账', path: '/sys/plots' },
  ]
  if (currentUser.value.role === '管理员') {
    menus.unshift({ title: '用户权限', path: '/sys/users' })
  }
  return menus
})

const isMember1Route = computed(() => route.path.startsWith('/sys'))
const isMember3Route = computed(() => route.path.startsWith('/iot'))
const isMember5Route = computed(() => route.path.startsWith('/member5'))
const userLabel = computed(() => currentUser.value.realName || currentUser.value.username || '已登录用户')

function readCachedUser() {
  try {
    return JSON.parse(localStorage.getItem('user-info') || '{}')
  } catch {
    return {}
  }
}

async function loadCurrentUser() {
  try {
    const user = await authApi.me()
    currentUser.value = user
    localStorage.setItem('user-info', JSON.stringify(user))
  } catch {
    localStorage.removeItem('sa-token')
    localStorage.removeItem('user-info')
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
  }
}

async function logout() {
  try {
    await authApi.logout()
  } catch {
    // Local logout still clears the invalid token.
  }
  localStorage.removeItem('sa-token')
  localStorage.removeItem('userId')
  localStorage.removeItem('user-info')
  ElMessage.success('已退出登录')
  router.replace('/login')
}

onMounted(loadCurrentUser)
</script>

<style scoped>
.app-frame {
  min-height: 100vh;
  padding: 20px 0 48px;
}

.topbar {
  position: sticky;
  top: 14px;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 18px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(248, 250, 241, 0.86);
  box-shadow: 0 14px 40px rgba(31, 54, 35, 0.12);
  backdrop-filter: blur(20px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 170px;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  background: var(--leaf-dark);
  color: var(--lime);
  font-weight: 900;
}

.brand b,
.brand small {
  display: block;
}

.brand small {
  color: rgba(23, 39, 28, 0.62);
  font-size: 12px;
}

.nav-links {
  display: flex;
  flex: 1;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
}

.user-box {
  display: grid;
  grid-template-columns: auto auto;
  align-items: center;
  gap: 2px 10px;
  min-width: 178px;
  padding: 8px 10px;
  border: 1px solid rgba(23, 39, 28, 0.08);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.62);
}

.user-box span {
  color: var(--leaf-dark);
  font-weight: 900;
}

.user-box small {
  color: rgba(23, 39, 28, 0.62);
}

.user-box .el-button {
  grid-row: span 2;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 999px;
  color: rgba(23, 39, 28, 0.74);
  font-size: 14px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.72);
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.nav-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(31, 54, 35, 0.08);
}

.nav-link.router-link-active,
.member-group.active > .member-link {
  background: var(--leaf);
  color: #fffdf2;
}

.member-group {
  position: relative;
}

.member-link {
  min-width: 112px;
  justify-content: center;
}

.arrow {
  font-style: normal;
  font-size: 12px;
  transition: transform 0.18s ease;
}

.member-group:hover .arrow,
.member-group:focus-within .arrow {
  transform: rotate(180deg);
}

.member-subnav {
  position: absolute;
  top: calc(100% + 10px);
  left: 50%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 156px;
  padding: 10px;
  border: 1px solid rgba(23, 39, 28, 0.06);
  border-radius: 14px;
  background: rgba(255, 253, 242, 0.98);
  box-shadow: 0 22px 56px rgba(31, 54, 35, 0.16);
  opacity: 0;
  pointer-events: none;
  transform: translateX(-50%) translateY(6px);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.member-group:hover .member-subnav,
.member-group:focus-within .member-subnav {
  opacity: 1;
  pointer-events: auto;
  transform: translateX(-50%) translateY(0);
}

.subnav-link {
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
  transition: background 0.18s ease, color 0.18s ease;
}

.subnav-link:hover,
.subnav-link.router-link-active {
  background: rgba(31, 111, 67, 0.1);
  color: var(--leaf-dark);
}

main {
  padding-top: 34px;
}

@media (max-width: 980px) {
  .topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .nav-links {
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
  }

  .user-box {
    width: 100%;
    grid-template-columns: 1fr auto;
  }
}

@media (max-width: 640px) {
  .nav-links {
    flex-wrap: wrap;
  }

  .member-subnav {
    left: 0;
    transform: translateX(0) translateY(6px);
  }

  .member-group:hover .member-subnav,
  .member-group:focus-within .member-subnav {
    transform: translateX(0) translateY(0);
  }
}
</style>
