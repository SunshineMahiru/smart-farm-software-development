import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layout/AppLayout.vue'
import HomeView from '../views/HomeView.vue'
import AlertManageView from '../views/iot/AlertManageView.vue'
import IotDashboardView from '../views/iot/IotDashboardView.vue'
import SensorHistoryView from '../views/iot/SensorHistoryView.vue'
import IotTwinView from '../views/iot/IotTwinView.vue'
import Member5Dashboard from '../views/member5/Member5Dashboard.vue'
import SupplierPage from '../views/member5/SupplierPage.vue'
import SensorPage from '../views/member5/SensorPage.vue'
import FarmLogPage from '../views/member5/FarmLogPage.vue'
import YieldStatPage from '../views/member5/YieldStatPage.vue'

const routes = [
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'iot', name: 'iot-dashboard', component: IotDashboardView },
      { path: 'iot/alerts', name: 'iot-alerts', component: AlertManageView },
      { path: 'iot/history', name: 'iot-history', component: SensorHistoryView },
      { path: 'iot/twin', name: 'iot-twin', component: IotTwinView },
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

export default router
