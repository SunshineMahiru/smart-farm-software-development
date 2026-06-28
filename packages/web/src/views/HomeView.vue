<template>
  <section class="home page-shell">
    <div class="hero panel">
      <div class="hero-copy">
        <p class="eyebrow">Smart Farm Digital Twin</p>
        <h1 class="page-title">智慧农场业务控制台</h1>
        <p class="hero-text">面向农场主与管理员的综合管理入口。当前先完成成员5负责的权限、供应商、传感器、农事日志与产量统计前端。</p>
        <div class="hero-actions">
          <RouterLink to="/member5" class="primary-link">进入成员5工作台</RouterLink>
          <RouterLink to="/member5/suppliers" class="ghost-link">查看供应商</RouterLink>
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
  { scope: 'Auth & CRUD', title: '成员5工作台', desc: '统一查看负责模块进度', path: '/member5' },
  { scope: 'Supply', title: '供应商管理', desc: '农资供应商档案与状态', path: '/member5/suppliers' },
  { scope: 'IoT CRUD', title: '传感器管理', desc: '设备基础信息与在线状态', path: '/member5/sensors' },
  { scope: 'Farm Ops', title: '农事日志', desc: '按计划记录日常操作', path: '/member5/farm-logs' },
  { scope: 'Yield', title: '产量统计', desc: '采收数据与计划汇总', path: '/member5/yields' },
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
.field-map span { min-height: 74px; border-radius: 8px; background: linear-gradient(135deg, rgba(184,219,99,.92), rgba(255,253,242,.32)); box-shadow: inset 0 0 0 1px rgba(255,255,255,.28), 0 12px 30px rgba(0,0,0,.18); animation: rise .7s ease both; animation-delay: var(--delay); }
.module-strip { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 14px; }
.module-card { padding: 18px; transition: transform .18s ease, box-shadow .18s ease; }
.module-card:hover { transform: translateY(-4px); box-shadow: 0 22px 56px rgba(31,54,35,.16); }
.module-card small { color: var(--clay); font-weight: 900; }
.module-card strong { display: block; margin: 8px 0; color: var(--leaf-dark); font-size: 20px; }
.module-card span { color: rgba(23,39,28,.66); font-size: 14px; }
@keyframes rise { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width: 980px) { .hero { grid-template-columns: 1fr; } .module-strip { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .module-strip { grid-template-columns: 1fr; } }
</style>
