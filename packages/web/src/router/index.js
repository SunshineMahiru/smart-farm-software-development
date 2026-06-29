import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layout/AppLayout.vue'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/login/LoginView.vue'
import AlertManageView from '../views/iot/AlertManageView.vue'
import IotDashboardView from '../views/iot/IotDashboardView.vue'
import SensorHistoryView from '../views/iot/SensorHistoryView.vue'
import IotTwinView from '../views/iot/IotTwinView.vue'
import Member5Dashboard from '../views/member5/Member5Dashboard.vue'
import FarmLogPage from '../views/member5/FarmLogPage.vue'
import SensorPage from '../views/member5/SensorPage.vue'
import SupplierPage from '../views/member5/SupplierPage.vue'
import YieldStatPage from '../views/member5/YieldStatPage.vue'
import ModulePlaceholderView from '../views/ModulePlaceholderView.vue'
import Member2PlanView from '../views/plan/Member2PlanView.vue'
import PlotManagePage from '../views/sys/PlotManagePage.vue'
import UserManagePage from '../views/sys/UserManagePage.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true },
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'sys/users', name: 'sys-users', component: UserManagePage, meta: { title: '用户权限管理' } },
      { path: 'sys/plots', name: 'sys-plots', component: PlotManagePage, meta: { title: '地块台账管理' } },
      { path: 'member2', name: 'member2', component: Member2PlanView, meta: { title: '实时调度与生命周期' } },
      { path: 'iot', name: 'iot-dashboard', component: IotDashboardView },
      { path: 'iot/alerts', name: 'iot-alerts', component: AlertManageView },
      { path: 'iot/history', name: 'iot-history', component: SensorHistoryView },
      { path: 'iot/twin', name: 'iot-twin', component: IotTwinView },
      { path: 'member4', name: 'member4', component: ModulePlaceholderView, meta: { title: '农资供应链与核心业务' } },
      { path: 'member5', name: 'member5', component: Member5Dashboard },
      { path: 'member5/suppliers', name: 'member5-suppliers', component: SupplierPage },
      { path: 'member5/sensors', name: 'member5-sensors', component: SensorPage },
      { path: 'member5/farm-logs', name: 'member5-farm-logs', component: FarmLogPage },
      { path: 'member5/yields', name: 'member5-yields', component: YieldStatPage },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  const token = localStorage.getItem('sa-token')
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && token) {
    return { path: '/' }
  }
  return true
})

export default router
