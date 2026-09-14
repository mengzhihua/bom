import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import { auth } from '../auth'

export const menus = [
  {
    path: '/dashboard',
    name: '工作台',
    icon: 'Odometer',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/parts',
    name: '零件管理',
    icon: 'Box',
    component: () => import('../views/parts/PartList.vue')
  },
  {
    path: '/boms',
    name: 'BOM 管理',
    icon: 'Files',
    component: () => import('../views/bom/BomList.vue')
  },
  {
    path: '/changes/ecr',
    name: 'ECR 工程变更',
    icon: 'EditPen',
    component: () => import('../views/change/EcrList.vue')
  },
  {
    path: '/changes/ecn',
    name: 'ECN 工程通知',
    icon: 'Bell',
    component: () => import('../views/change/EcnList.vue')
  },
  {
    path: '/integration',
    name: '集成日志',
    icon: 'Connection',
    component: () => import('../views/integration/IntegrationLogs.vue')
  },
  {
    path: '/master/models',
    name: '车型项目',
    icon: 'Van',
    component: () => import('../views/master/VehicleModels.vue')
  },
  {
    path: '/master/plants',
    name: '工厂',
    icon: 'OfficeBuilding',
    component: () => import('../views/master/Plants.vue')
  },
  {
    path: '/master/suppliers',
    name: '供应商',
    icon: 'Briefcase',
    component: () => import('../views/master/Suppliers.vue')
  },
  {
    path: '/master/features',
    name: '配置特征',
    icon: 'Collection',
    component: () => import('../views/master/Features.vue')
  },
  {
    path: '/master/workstations',
    name: '工位',
    icon: 'Operation',
    component: () => import('../views/master/Workstations.vue')
  },
  {
    path: '/system/users',
    name: '用户管理',
    icon: 'User',
    component: () => import('../views/system/Users.vue')
  },
  {
    path: '/system/oplog',
    name: '操作日志',
    icon: 'Tickets',
    component: () => import('../views/system/OpLog.vue')
  }
]

export const visibleMenus = () => menus
export const homePath = () => '/dashboard'

const detailRoutes = [
  {
    path: 'parts/:id',
    name: '零件详情',
    component: () => import('../views/parts/PartDetail.vue'),
    meta: { menu: '零件管理' }
  },
  {
    path: 'boms/compare',
    name: 'BOM 对比',
    component: () => import('../views/bom/BomCompare.vue'),
    meta: { menu: 'BOM 管理' }
  },
  {
    path: 'boms/:id',
    name: 'BOM 详情',
    component: () => import('../views/bom/BomDetail.vue'),
    meta: { menu: 'BOM 管理' }
  },
  {
    path: 'ecns/:id',
    name: 'ECN 详情',
    component: () => import('../views/change/EcnDetail.vue'),
    meta: { menu: 'ECN 工程通知' }
  }
]

const routes = [
  {
    path: '/login',
    name: '登录',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: homePath,
    children: [
      ...menus.map((menu) => ({
        path: menu.path.substring(1),
        name: menu.name,
        component: menu.component
      })),
      ...detailRoutes
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.path === '/login') {
    return auth.token ? homePath() : true
  }
  if (!auth.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }
  return true
})

export default router
