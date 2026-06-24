import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'order-create', name: 'OrderCreate', component: () => import('@/views/OrderCreate.vue'), meta: { title: '下单' } },
      { path: 'order-list', name: 'OrderList', component: () => import('@/views/OrderList.vue'), meta: { title: '订单' } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'trip/:id', name: 'TripTracking', component: () => import('@/views/TripTracking.vue'), meta: { title: '行程追踪' } },
      { path: 'address', name: 'AddressManage', component: () => import('@/views/AddressManage.vue'), meta: { title: '收藏地址' } },
      { path: 'coupon', name: 'CouponCenter', component: () => import('@/views/CouponCenter.vue'), meta: { title: '优惠券' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '我的' } }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  {
    path: '/driver',
    component: () => import('@/views/DriverLayout.vue'),
    redirect: '/driver/home',
    children: [
      { path: 'home', name: 'DriverHome', component: () => import('@/views/DriverHome.vue'), meta: { title: '司机端' } },
      { path: 'order/:id', name: 'DriverOrder', component: () => import('@/views/DriverOrder.vue'), meta: { title: '订单详情' } },
      { path: 'earnings', name: 'DriverEarnings', component: () => import('@/views/DriverEarnings.vue'), meta: { title: '收入' } },
      { path: 'profile', name: 'DriverProfile', component: () => import('@/views/DriverProfile.vue'), meta: { title: '个人' } }
    ]
  },
  { path: '/driver/login', name: 'DriverLogin', component: () => import('@/views/DriverLogin.vue'), meta: { title: '司机登录' } },
  {
    path: '/admin',
    component: () => import('@/views/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/AdminDashboard.vue'), meta: { title: '管理后台' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/AdminUsers.vue'), meta: { title: '用户管理' } },
      { path: 'drivers', name: 'AdminDrivers', component: () => import('@/views/AdminDrivers.vue'), meta: { title: '司机审核' } },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/AdminOrders.vue'), meta: { title: '订单监控' } },
      { path: 'alerts', name: 'AdminAlerts', component: () => import('@/views/AdminAlerts.vue'), meta: { title: '风控告警' } },
      { path: 'car-types', name: 'AdminCarTypes', component: () => import('@/views/AdminCarTypes.vue'), meta: { title: '定价管理' } }
    ]
  },
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/AdminLogin.vue'), meta: { title: '管理员登录' } }
]

// 需要登录认证的路由名称列表
const authRequiredRoutes = [
  'OrderCreate', 'OrderList', 'OrderDetail', 'TripTracking',
  'AddressManage', 'CouponCenter', 'Profile'
]

// 需要司机登录的路由名称列表
const driverAuthRoutes = [
  'DriverHome', 'DriverOrder', 'DriverEarnings', 'DriverProfile'
]

// 需要管理员登录的路由名称列表
const adminAuthRoutes = [
  'AdminDashboard', 'AdminUsers', 'AdminDrivers',
  'AdminOrders', 'AdminAlerts', 'AdminCarTypes'
]

router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 江南出行`
  }

  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')
  const driverToken = localStorage.getItem('driverToken')

  // 管理员路由鉴权
  if (adminAuthRoutes.includes(to.name)) {
    if (!adminToken) {
      ElMessage.warning('请先登录管理后台')
      return next('/admin/login')
    }
    return next()
  }

  // 司机路由鉴权
  if (driverAuthRoutes.includes(to.name)) {
    if (!driverToken) {
      ElMessage.warning('请先登录司机端')
      return next('/driver/login')
    }
    return next()
  }

  // 乘客路由鉴权
  if (authRequiredRoutes.includes(to.name)) {
    if (!token) {
      ElMessage.warning('请先登录')
      return next('/login')
    }
    return next()
  }

  next()
})

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
