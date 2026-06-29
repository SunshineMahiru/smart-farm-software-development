import request from '../utils/request'

export const authApi = {
  login: data => request.post('/sys/user/login', data),
  logout: () => request.post('/sys/user/logout'),
  me: () => request.get('/sys/user/me'),
}

export const member1Api = {
  users: params => request.get('/sys/user', { params }),
  createUser: data => request.post('/sys/user', data),
  updateUser: (userId, data) => request.put(`/sys/user/${userId}`, data),
  resetPassword: (userId, data) => request.patch(`/sys/user/${userId}/password`, data),
  deleteUser: userId => request.delete(`/sys/user/${userId}`),
  plots: params => request.get('/farm/plots', { params }),
  plotSummary: () => request.get('/farm/plots/status-summary'),
  createPlot: data => request.post('/farm/plots', data),
  updatePlot: (plotId, data) => request.put(`/farm/plots/${plotId}`, data),
  updatePlotStatus: (plotId, status) => request.patch(`/farm/plots/${plotId}/status`, { status }),
  deletePlot: plotId => request.delete(`/farm/plots/${plotId}`),
}
