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

export const dashboard = { summary: () => http.get('/dashboard/summary') }
export const master = {
  models: () => http.get('/master/vehicle-models'), model: (d) => http.post('/master/vehicle-models', d),
  plants: () => http.get('/master/plants'), plant: (d) => http.post('/master/plants', d),
  suppliers: () => http.get('/master/suppliers'), supplier: (d) => http.post('/master/suppliers', d),
  features: () => http.get('/master/features'), feature: (d) => http.post('/master/features', d),
  options: (id) => http.get(`/master/features/${id}/options`), option: (id, d) => http.post(`/master/features/${id}/options`, d),
  workstations: () => http.get('/master/workstations'), workstation: (d) => http.post('/master/workstations', d)
}
export const parts = {
  page: (p) => http.get('/parts', { params: p }), get: (id) => http.get(`/parts/${id}`),
  create: (d) => http.post('/parts', d), update: (id, d) => http.put(`/parts/${id}`, d),
  remove: (id) => http.delete(`/parts/${id}`), action: (id, n) => http.post(`/parts/${id}/${n}`),
  documents: (id) => http.get(`/parts/${id}/documents`), addDocument: (id, d) => http.post(`/parts/${id}/documents`, d),
  deleteDocument: (id) => http.delete(`/parts/documents/${id}`), whereUsed: (id, recursive = true) => http.get(`/parts/${id}/where-used`, { params: { recursive } })
}
export const boms = {
  list: () => http.get('/boms'), get: (id) => http.get(`/boms/${id}`), create: (d) => http.post('/boms', d),
  items: (id) => http.get(`/boms/${id}/items`), addItem: (id, d) => http.post(`/boms/${id}/items`, d),
  updateItem: (id, itemId, d) => http.put(`/boms/${id}/items/${itemId}`, d), deleteItem: (id, itemId) => http.delete(`/boms/${id}/items/${itemId}`),
  tree: (id) => http.get(`/boms/${id}/tree`), explode: (id, level) => http.get(`/boms/${id}/explode`, { params: { level } }),
  summarized: (id) => http.get(`/boms/${id}/summarized`), rollup: (id) => http.get(`/boms/${id}/rollup`),
  action: (id, name, d) => http.post(`/boms/${id}/${name}`, d), compare: (l, r) => http.get('/boms/compare', { params: { leftId: l, rightId: r } })
}
export const changes = {
  ecrs: () => http.get('/ecrs'), createEcr: (d) => http.post('/ecrs', d), ecrAction: (id, a) => http.post(`/ecrs/${id}/${a}`),
  ecns: () => http.get('/ecns'), ecn: (id) => http.get(`/ecns/${id}`), updateEcn: (id, d) => http.put(`/ecns/${id}`, d),
  ecnItems: (id) => http.get(`/ecns/${id}/items`), addEcnItem: (id, d) => http.post(`/ecns/${id}/items`, d), ecnAction: (id, a) => http.post(`/ecns/${id}/${a}`)
}
export const integration = { logs: (p) => http.get('/integration/logs', { params: p }), retry: (id) => http.post(`/integration/logs/${id}/retry`) }
export const system = { users: crud('/system/user'), oplog: (p) => http.get('/system/oplog/page', { params: p }) }
