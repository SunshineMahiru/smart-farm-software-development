<template>
  <section class="home page-shell">
    <div class="hero panel">
      <div class="hero-copy">
        <p class="eyebrow">Smart Farm Digital Twin</p>
        <h1 class="page-title">智慧农场业务控制台</h1>
        <p class="hero-text">
          面向农场主与管理员的综合管理入口。五个模块分支按团队职责拆分，已接入的模块可进入真实业务页面，未接入的模块先提供清晰入口与占位页面。
        </p>
        <div class="hero-actions">
          <RouterLink to="/iot" class="primary-link">进入 IoT 孪生与 AI 算法</RouterLink>
          <RouterLink to="/sys/users" class="ghost-link">系统基建与空间资产</RouterLink>
        </div>
      </div>
      <div class="field-map">
        <span v-for="item in 18" :key="item" :style="{ '--delay': `${item * 45}ms` }" />
      </div>
    </div>

    <div class="module-strip">
      <RouterLink v-for="item in modules" :key="item.path" :to="item.path" class="module-card panel">
        <small>{{ item.scope }}</small>
        <strong>{{ item.title }}</strong>
        <span>{{ item.desc }}</span>
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'

const modules = [
  { scope: '成员1', title: '系统基建与空间资产', desc: '账号权限、动态菜单、用户管理与地块台账', path: '/sys/users' },
  { scope: '成员2', title: '实时调度与生命周期', desc: '作物周期、种植计划与调度通知入口', path: '/member2' },
  { scope: '成员3', title: 'IoT 孪生与 AI 算法', desc: '传感器状态、热力图、告警与农情日报', path: '/iot' },
  { scope: '成员4', title: '农资供应链与核心业务', desc: '农资供应、库存、采购入库与业务一致性入口', path: '/member4' },
  { scope: '成员5', title: '农场作业与可视化大屏', desc: '农事日志、产量统计与经营看板', path: '/member5' },
]
</script>

<style scoped>
.home { display: grid; gap: 20px; }
.hero { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(320px, .9fr); min-height: 470px; overflow: hidden; }
.hero-copy { padding: clamp(28px, 6vw, 68px); }
.hero-text { max-width: 610px; margin: 18px 0 0; color: rgba(23,39,28,.72); font-size: 18px; line-height: 1.8; }
.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 30px; }
.primary-link, .ghost-link { padding: 13px 18px; border-radius: 8px; font-weight: 900; }
.primary-link { background: var(--leaf); color: #fffdf2; }
.ghost-link { border: 1px solid var(--line); background: rgba(255,255,255,.55); color: var(--leaf-dark); }
.field-map { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; padding: 34px; background: linear-gradient(140deg, rgba(18,61,43,.96), rgba(31,111,67,.86)); transform: skewY(-4deg); transform-origin: center; }
.field-map span { border-radius: 8px; min-height: 76px; background: linear-gradient(135deg, rgba(184,219,99,.9), rgba(255,253,242,.26)); animation: pulse 2.8s ease-in-out infinite; animation-delay: var(--delay); }
.module-strip { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 14px; }
.module-card { display: grid; gap: 10px; padding: 20px; min-height: 156px; transition: transform .18s ease, box-shadow .18s ease; }
.module-card:hover { transform: translateY(-5px); box-shadow: 0 22px 58px rgba(31,54,35,.16); }
.module-card small { color: var(--clay); font-weight: 900; }
.module-card strong { color: var(--leaf-dark); font-size: 21px; line-height: 1.25; }
.module-card span { color: rgba(23,39,28,.66); line-height: 1.65; }
@keyframes pulse { 50% { transform: translateY(-4px); filter: brightness(1.08); } }
@media (max-width: 980px) { .hero { grid-template-columns: 1fr; } .module-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 560px) { .module-strip { grid-template-columns: 1fr; } }
</style>
