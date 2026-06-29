<template>
  <section class="panel data-panel member1-panel">
    <div class="section-head">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p v-if="description" class="muted">{{ description }}</p>
      </div>
      <slot name="actions" />
    </div>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, default: 'Member 1' },
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.member1-panel {
  position: relative;
  overflow: hidden;
}

.member1-panel::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  background: linear-gradient(180deg, #123d2b, var(--leaf), var(--lime));
}
</style>
