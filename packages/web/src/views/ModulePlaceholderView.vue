<template>
  <section class="page-shell placeholder-page">
    <div class="placeholder-card panel">
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1 class="page-title">{{ title }}</h1>
      <p class="muted placeholder-desc">{{ description }}</p>

      <div class="placeholder-grid">
        <div v-for="item in items" :key="item" class="placeholder-item">
          <span>{{ item }}</span>
        </div>
      </div>

      <RouterLink to="/" class="back-link">返回首页</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const pageMap = {
  member2: {
    eyebrow: 'Member 2 Module',
    title: '实时调度与生命周期',
    description: '该模块入口已预留，后续可接入作物字典、种植计划、生命周期计算、定时任务和 WebSocket 通知页面。',
    items: ['作物字典', '种植计划', '生命周期计算', '实时调度通知'],
  },
  member4: {
    eyebrow: 'Member 4 Module',
    title: '农资供应链与核心业务',
    description: '该模块入口已预留，后续可接入农资档案、库存管理、采购入库、库存预警和业务流水追溯页面。',
    items: ['农资档案', '库存管理', '采购入库', '供应链追溯'],
  },
}

const current = computed(() => pageMap[route.name] || pageMap.member2)
const eyebrow = computed(() => current.value.eyebrow)
const title = computed(() => current.value.title)
const description = computed(() => current.value.description)
const items = computed(() => current.value.items)
</script>

<style scoped>
.placeholder-page {
  display: grid;
  gap: 18px;
}

.placeholder-card {
  padding: clamp(28px, 5vw, 56px);
}

.placeholder-desc {
  max-width: 760px;
  margin: 18px 0 0;
  font-size: 17px;
  line-height: 1.8;
}

.placeholder-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.placeholder-item {
  min-height: 112px;
  display: grid;
  place-items: center;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.58);
  color: var(--leaf-dark);
  font-weight: 900;
  text-align: center;
}

.back-link {
  display: inline-flex;
  margin-top: 28px;
  padding: 12px 18px;
  border-radius: 8px;
  background: var(--leaf);
  color: #fffdf2;
  font-weight: 900;
}

@media (max-width: 840px) {
  .placeholder-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .placeholder-grid {
    grid-template-columns: 1fr;
  }
}
</style>
