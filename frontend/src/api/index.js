import http from './request'

const crud = (base) => ({
  list: (params) => http.get(base, { params }),
  get: (id) => http.get(`${base}/${id}`),
  create: (data) => http.post(base, data),
  update: (id, data) => http.put(`${base}/${id}`, data),
  remove: (id) => http.delete(`${base}/${id}`)
})

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),
  changePassword: (data) => http.post('/auth/password', data),
  logout: () => http.post('/auth/logout')
}

export const dashboard = {
  summary: () => http.get('/dashboard/summary')
}

export const master = {
  models: () => http.get('/master/vehicle-models'),
  model: (data) => http.post('/master/vehicle-models', data),
  plants: () => http.get('/master/plants'),
  plant: (data) => http.post('/master/plants', data),
  suppliers: () => http.get('/master/suppliers'),
  supplier: (data) => http.post('/master/suppliers', data),
  features: () => http.get('/master/features'),
  feature: (data) => http.post('/master/features', data),
  options: (id) => http.get(`/master/features/${id}/options`),
  option: (id, data) => http.post(`/master/features/${id}/options`, data),
  deleteOption: (id) => http.delete(`/master/features/options/${id}`),
  workstations: (params) => http.get('/master/workstations', { params }),
  workstation: (data) => http.post('/master/workstations', data)
}

export const parts = {
  page: (params) => http.get('/parts', { params }),
  get: (id) => http.get(`/parts/${id}`),
  create: (data) => http.post('/parts', data),
  update: (id, data) => http.put(`/parts/${id}`, data),
  remove: (id) => http.delete(`/parts/${id}`),
  action: (id, name) => http.post(`/parts/${id}/${name}`),
  documents: (id) => http.get(`/parts/${id}/documents`),
  addDocument: (id, data) => http.post(`/parts/${id}/documents`, data),
  deleteDocument: (id) => http.delete(`/parts/documents/${id}`),
  whereUsed: (id, recursive = true) => http.get(
    `/parts/${id}/where-used`,
    { params: { recursive } }
  ),
  importCsv: (file) => {
    const data = new FormData()
    data.append('file', file)
    return http.post('/parts/import', data)
  },
  exportCsv: () => http.get('/parts/export', { responseType: 'blob' })
}

export const boms = {
  list: () => http.get('/boms'),
  get: (id) => http.get(`/boms/${id}`),
  create: (data) => http.post('/boms', data),
  items: (id) => http.get(`/boms/${id}/items`),
  addItem: (id, data) => http.post(`/boms/${id}/items`, data),
  updateItem: (id, itemId, data) => http.put(`/boms/${id}/items/${itemId}`, data),
  deleteItem: (id, itemId) => http.delete(`/boms/${id}/items/${itemId}`),
  tree: (id) => http.get(`/boms/${id}/tree`),
  explode: (id, level) => http.get(`/boms/${id}/explode`, { params: { level } }),
  summarized: (id) => http.get(`/boms/${id}/summarized`),
  rollup: (id) => http.get(`/boms/${id}/rollup`),
  byStation: (id) => http.get(`/boms/${id}/by-station`),
  configure: (id, data) => http.post(`/boms/${id}/configure`, data),
  action: (id, name, data) => http.post(`/boms/${id}/${name}`, data),
  compare: (leftId, rightId) => http.get(
    '/boms/compare',
    { params: { leftId, rightId } }
  ),
  importCsv: (id, file) => {
    const data = new FormData()
    data.append('file', file)
    return http.post(`/boms/${id}/import`, data)
  },
  exportCsv: (id) => http.get(`/boms/${id}/export`, { responseType: 'blob' })
}

export const changes = {
  ecrs: () => http.get('/ecrs'),
  createEcr: (data) => http.post('/ecrs', data),
  ecrAction: (id, action, data) => http.post(`/ecrs/${id}/${action}`, data),
  ecns: () => http.get('/ecns'),
  ecn: (id) => http.get(`/ecns/${id}`),
  updateEcn: (id, data) => http.put(`/ecns/${id}`, data),
  ecnItems: (id) => http.get(`/ecns/${id}/items`),
  addEcnItem: (id, data) => http.post(`/ecns/${id}/items`, data),
  ecnAction: (id, action, data) => http.post(`/ecns/${id}/${action}`, data)
}

export const integration = {
  logs: (params) => http.get('/integration/logs', { params }),
  retry: (id) => http.post(`/integration/logs/${id}/retry`)
}

export const system = {
  users: crud('/system/user'),
  oplog: (params) => http.get('/system/oplog/page', { params })
}
