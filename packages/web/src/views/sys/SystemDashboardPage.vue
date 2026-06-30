<template>
  <section class="page-shell sys-dashboard">
    <div class="sys-hero panel">
      <div class="hero-copy">
        <p class="eyebrow">Member 1 Foundation</p>
        <h1 class="page-title">系统基建与空间资产工作台</h1>
        <p class="hero-text">
          统一查看登录态、角色权限、用户结构和地块资产状态。成员1负责为其他模块提供稳定的账号、权限、空间资产与 WebSocket 基础底座。
        </p>
        <div class="hero-actions">
          <RouterLink to="/sys/users" class="primary-link" :class="{ disabled: !isAdmin }">用户权限管理</RouterLink>
          <RouterLink to="/sys/plots" class="ghost-link">地块台账管理</RouterLink>
        </div>
      </div>

      <div class="security-card">
        <span class="security-badge">{{ currentUser.role || '未识别角色' }}</span>
        <strong>{{ currentUser.realName || currentUser.username || '已登录用户' }}</strong>
        <small>Sa-Token 已接入 · Authorization 请求头 · 路由守卫已启用</small>
        <div class="security-lines">
          <i v-for="item in 9" :key="item" :style="{ '--delay': `${item * 60}ms` }" />
        </div>
      </div>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card foundation-metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </div>
    </div>

    <div class="dashboard-grid">
      <Member1DataPanel title="权限治理视图" description="按当前登录角色展示可操作能力，帮助答辩时说明 RBAC 体系如何保护核心资产。">
        <div class="permission-board">
          <div v-for="item in permissionCards" :key="item.title" class="permission-card" :class="{ locked: item.locked }">
            <span>{{ item.scope }}</span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.desc }}</small>
          </div>
        </div>
      </Member1DataPanel>

      <Member1DataPanel title="地块状态结构" description="地块台账为种植计划、IoT 设备、农事日志和产量统计提供共享空间基准。">
        <div class="plot-radar">
          <div v-for="item in plotCards" :key="item.status" class="plot-node" :class="`status-${item.status}`">
            <span>{{ item.status }}</span>
            <strong>{{ item.totalCount }}</strong>
            <small>{{ item.totalArea }} 亩</small>
          </div>
        </div>
      </Member1DataPanel>
    </div>

    <div class="workflow panel">
      <div class="workflow-head">
        <p class="eyebrow">System Workflow</p>
        <h2>成员一支撑链路</h2>
      </div>
      <div class="workflow-line">
        <div v-for="item in workflow" :key="item.title" class="workflow-step">
          <span>{{ item.index }}</span>
          <strong>{{ item.title }}</strong>
          <small>{{ item.desc }}</small>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import Member1DataPanel from '../../components/Member1DataPanel.vue'
import { authApi, member1Api } from '../../api/member1'

const currentUser = ref(readCachedUser())
const userPage = ref({ records: [], total: 0 })
const plotSummary = ref([])

const isAdmin = computed(() => currentUser.value?.role === '管理员')
const userRecords = computed(() => userPage.value.records || [])
const adminCount = computed(() => userRecords.value.filter(item => item.role === '管理员').length)
const techCount = computed(() => userRecords.value.filter(item => item.role === '农技员').length)
const totalArea = computed(() => plotSummary.value.reduce((sum, item) => sum + Number(item.totalArea || 0), 0).toFixed(2))
const totalPlots = computed(() => plotSummary.value.reduce((sum, item) => sum + Number(item.totalCount || 0), 0))

const metrics = computed(() => [
  { label: '用户账号', value: userPage.value.total || 0, hint: `管理员 ${adminCount.value} · 农技员 ${techCount.value}` },
  { label: '地块资产', value: totalPlots.value, hint: `总面积 ${totalArea.value} 亩` },
  { label: '当前角色', value: currentUser.value.role || '--', hint: isAdmin.value ? '具备用户与地块写权限' : '只开放查询能力' },
  { label: '基建状态', value: '已接入', hint: '登录鉴权 / 路由守卫 / WebSocket' },
])

const permissionCards = computed(() => [
  { scope: 'Auth', title: '登录态校验', desc: '未登录访问业务页面自动跳转登录页。', locked: false },
  { scope: 'RBAC', title: '用户写操作', desc: isAdmin.value ? '管理员可新增、编辑、删除与重置密码。' : '非管理员仅可查看。', locked: !isAdmin.value },
  { scope: 'Spatial', title: '地块状态流转', desc: isAdmin.value ? '管理员可维护空间资产状态。' : '非管理员仅可查看地块。', locked: !isAdmin.value },
  { scope: 'Safety', title: '业务引用保护', desc: '删除用户或地块前由后端检查业务引用。', locked: false },
])

const plotCards = computed(() => {
  const statuses = ['空闲', '种植中', '休耕', '维护中']
  return statuses.map(status => plotSummary.value.find(item => item.status === status) || { status, totalCount: 0, totalArea: 0 })
})

const workflow = [
  { index: '01', title: '登录授权', desc: 'Sa-Token 下发 Token，前端统一挂载 Authorization。' },
  { index: '02', title: '角色判定', desc: '管理员开放写操作，农技员保留查询入口。' },
  { index: '03', title: '资产维护', desc: '用户账号与地块台账形成全组共享基础数据。' },
  { index: '04', title: '业务支撑', desc: '种植、IoT、农事和产量模块复用用户与地块。' },
]

function readCachedUser() {
  try {
    return JSON.parse(localStorage.getItem('user-info') || '{}')
  } catch {
    return {}
  }
}

async function loadDashboard() {
  const [user, users, summary] = await Promise.all([
    authApi.me(),
    member1Api.users({ pageNum: 1, pageSize: 100 }),
    member1Api.plotSummary(),
  ])
  currentUser.value = user
  userPage.value = users || { records: [], total: 0 }
  plotSummary.value = summary || []
  localStorage.setItem('user-info', JSON.stringify(user))
}

onMounted(loadDashboard)
</script>

<style scoped>
.sys-dashboard {
  display: grid;
  gap: 18px;
}

.sys-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  min-height: 420px;
  overflow: hidden;
  padding: clamp(28px, 5vw, 54px);
  background:
    radial-gradient(circle at 20% 18%, rgba(184, 219, 99, 0.35), transparent 20rem),
    linear-gradient(135deg, rgba(255, 255, 250, 0.9), rgba(223, 238, 218, 0.86));
}

.hero-copy {
  align-self: center;
}

.hero-text {
  max-width: 720px;
  margin: 18px 0 0;
  color: rgba(23, 39, 28, 0.72);
  font-size: 17px;
  line-height: 1.85;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.primary-link,
.ghost-link {
  padding: 13px 18px;
  border-radius: 8px;
  font-weight: 900;
}

.primary-link {
  background: var(--leaf);
  color: #fffdf2;
}

.primary-link.disabled {
  opacity: 0.58;
}

.ghost-link {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.62);
  color: var(--leaf-dark);
}

.security-card {
  position: relative;
  align-self: stretch;
  display: grid;
  align-content: end;
  gap: 12px;
  min-height: 320px;
  padding: 26px;
  border-radius: 18px;
  background: linear-gradient(145deg, rgba(18, 61, 43, 0.96), rgba(31, 111, 67, 0.88));
  color: #fffdf2;
  overflow: hidden;
  box-shadow: 0 28px 70px rgba(18, 61, 43, 0.28);
}

.security-badge {
  width: fit-content;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: var(--lime);
  font-weight: 900;
}

.security-card strong {
  font-size: 34px;
  line-height: 1;
}

.security-card small {
  color: rgba(255, 253, 242, 0.72);
  line-height: 1.6;
}

.security-lines {
  position: absolute;
  inset: 24px 24px auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  transform: perspective(520px) rotateX(58deg) rotateZ(-12deg);
}

.security-lines i {
  min-height: 58px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(184, 219, 99, 0.9), rgba(255, 253, 242, 0.18));
  animation: tile-breathe 2.4s ease-in-out infinite;
  animation-delay: var(--delay);
}

.foundation-metric {
  min-height: 138px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.permission-board,
.plot-radar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.permission-card,
.plot-node {
  display: grid;
  gap: 8px;
  min-height: 132px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.58);
}

.permission-card span,
.plot-node span {
  color: var(--clay);
  font-weight: 900;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.permission-card strong,
.plot-node strong {
  color: var(--leaf-dark);
  font-size: 24px;
}

.permission-card small,
.plot-node small {
  color: rgba(23, 39, 28, 0.66);
  line-height: 1.6;
}

.permission-card.locked {
  background: rgba(255, 255, 255, 0.36);
  opacity: 0.72;
}

.plot-node {
  border-left: 5px solid var(--leaf);
}

.status-空闲 { border-left-color: #7b8794; }
.status-种植中 { border-left-color: var(--leaf); }
.status-休耕 { border-left-color: var(--clay); }
.status-维护中 { border-left-color: #b44747; }

.workflow {
  padding: 24px;
}

.workflow-head h2 {
  margin: 0;
  color: var(--leaf-dark);
}

.workflow-line {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.workflow-step {
  display: grid;
  gap: 10px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.52);
}

.workflow-step span {
  color: var(--clay);
  font-weight: 900;
}

.workflow-step strong {
  color: var(--leaf-dark);
}

.workflow-step small {
  color: rgba(23, 39, 28, 0.66);
  line-height: 1.6;
}

@keyframes tile-breathe {
  50% { transform: translateY(-5px); filter: brightness(1.12); }
}

@media (max-width: 980px) {
  .sys-hero,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .workflow-line {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .permission-board,
  .plot-radar,
  .workflow-line {
    grid-template-columns: 1fr;
  }
}
</style>
