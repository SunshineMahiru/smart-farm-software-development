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
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/member5">成员5总览</RouterLink>
        <RouterLink to="/member5/suppliers">供应商</RouterLink>
        <RouterLink to="/member5/sensors">传感器</RouterLink>
        <RouterLink to="/member5/farm-logs">农事日志</RouterLink>
        <RouterLink to="/member5/yields">产量统计</RouterLink>
      </nav>

      <div class="user-pill">Member 5</div>
    </header>

    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
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
  justify-content: space-between;
  gap: 18px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: rgba(248, 250, 241, 0.82);
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
.brand small { display: block; }
.brand small { color: rgba(23, 39, 28, 0.62); font-size: 12px; }

.nav-links {
  display: flex;
  flex: 1;
  justify-content: center;
  gap: 8px;
  overflow-x: auto;
}

.nav-links a {
  flex: 0 0 auto;
  padding: 10px 12px;
  border-radius: 8px;
  color: rgba(23, 39, 28, 0.72);
  font-size: 14px;
  font-weight: 700;
}

.nav-links a.router-link-active {
  background: var(--leaf);
  color: #fffdf2;
}

.user-pill {
  flex: 0 0 auto;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(184, 219, 99, 0.4);
  color: var(--leaf-dark);
  font-weight: 800;
}

main { padding-top: 34px; }

@media (max-width: 980px) {
  .topbar { align-items: flex-start; flex-direction: column; }
  .nav-links { width: 100%; justify-content: flex-start; }
  .user-pill { display: none; }
}
</style>
