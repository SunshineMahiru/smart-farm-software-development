import request from '../utils/request'

export const member2Api = {
  crops: params => request.get('/plan/crops', { params }),
  cropOptions: () => request.get('/plan/crops/options'),
  createCrop: data => request.post('/plan/crops', data),
  updateCrop: (cropId, data) => request.put(`/plan/crops/${cropId}`, data),
  deleteCrop: cropId => request.delete(`/plan/crops/${cropId}`),

  plans: params => request.get('/plan/plans', { params }),
  planDetail: planId => request.get(`/plan/plans/${planId}`),
  planCalendar: params => request.get('/plan/plans/calendar', { params }),
  reminders: () => request.get('/plan/plans/reminders'),
  createPlan: data => request.post('/plan/plans', data),
  updatePlan: (planId, data) => request.put(`/plan/plans/${planId}`, data),
  deletePlan: planId => request.delete(`/plan/plans/${planId}`),

  plots: params => request.get('/farm/plots', { params }),
}
