import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 解码 JWT 并检查是否过期
 * @param {string} token JWT token
 * @returns {boolean} true=已过期或无效, false=未过期
 */
function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    // exp 是 Unix 时间戳（秒），当前时间需转为秒
    return Date.now() >= payload.exp * 1000
  } catch {
    // 无法解码的 token 视为过期
    return true
  }
}

/** 清除所有登录状态 */
function clearAllAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  localStorage.removeItem('driverToken')
  localStorage.removeItem('driverInfo')
}

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
      { path: 'order/:id/review', name: 'OrderReview', component: () => import('@/views/ReviewOrder.vue'), meta: { title: '评价订单' } },
      { path: 'payment/:id', name: 'Payment', component: () => import('@/views/Payment.vue'), meta: { title: '支付' } },
      { path: 'invoice-center', name: 'InvoiceCenter', component: () => import('@/views/InvoiceCenter.vue'), meta: { title: '发票中心' } },
      { path: 'invoice-apply', name: 'InvoiceApply', component: () => import('@/views/InvoiceApply.vue'), meta: { title: '申请发票' } },
      { path: 'bus-line', name: 'BusLine', component: () => import('@/views/BusLine.vue'), meta: { title: '城际班线' } },
      { path: 'trip/:id', name: 'TripTracking', component: () => import('@/views/TripTracking.vue'), meta: { title: '行程追踪' } },
      { path: 'address', name: 'AddressManage', component: () => import('@/views/AddressManage.vue'), meta: { title: '收藏地址' } },
      { path: 'coupon', name: 'CouponCenter', component: () => import('@/views/CouponCenter.vue'), meta: { title: '优惠券' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '我的' } },
      { path: 'security-settings', name: 'SecuritySettings', component: () => import('@/views/SecuritySettings.vue'), meta: { title: '安全设置' } },
      { path: 'about-company', name: 'AboutCompany', component: () => import('@/views/AboutCompany.vue'), meta: { title: '关于我们' } },
      { path: 'message-center', name: 'MessageCenter', component: () => import('@/views/MessageCenter.vue'), meta: { title: '消息中心' } },
      { path: 'campaign-list', name: 'CampaignList', component: () => import('@/views/CampaignList.vue'), meta: { title: '活动中心' } },
      { path: 'campaign/:id', name: 'CampaignDetail', component: () => import('@/views/CampaignDetail.vue'), meta: { title: '活动详情' } },
      { path: 'vip-center', name: 'VipCenter', component: () => import('@/views/VipCenter.vue'), meta: { title: '会员中心' } },
      { path: 'landmark-explore', name: 'LandmarkExplore', component: () => import('@/views/LandmarkExplore.vue'), meta: { title: '发现' } },
      { path: 'ai-assistant', name: 'AiAssistant', component: () => import('@/views/AiAssistant.vue'), meta: { title: 'AI助手' } }
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
  { path: '/driver/login', redirect: '/login' },
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
      { path: 'car-types', name: 'AdminCarTypes', component: () => import('@/views/AdminCarTypes.vue'), meta: { title: '定价管理' } },
      { path: 'campaigns', name: 'AdminCampaigns', component: () => import('@/views/AdminCampaigns.vue'), meta: { title: '活动管理' } },
      { path: 'vip-levels', name: 'AdminVipLevels', component: () => import('@/views/AdminVipLevels.vue'), meta: { title: 'VIP等级' } },
      { path: 'bus-lines', name: 'AdminBusLines', component: () => import('@/views/AdminBusLines.vue'), meta: { title: '班线管理' } },
      { path: 'profile', name: 'AdminProfile', component: () => import('@/views/AdminProfile.vue'), meta: { title: '个人资料' } }
    ]
  },
  { path: '/admin/login', redirect: '/login' },
  { path: '/orders', redirect: '/order-list' }
]

// 需要登录认证的路由名称列表
const authRequiredRoutes = [
  'Home', 'OrderCreate', 'OrderList', 'OrderDetail', 'OrderReview', 'TripTracking',
  'AddressManage', 'CouponCenter', 'Profile',
  'SecuritySettings', 'AboutCompany', 'MessageCenter',
  'CampaignList', 'CampaignDetail', 'VipCenter', 'LandmarkExplore', 'AiAssistant', 'Payment', 'InvoiceCenter', 'InvoiceApply', 'BusLine'
]

// 需要司机登录的路由名称列表
const driverAuthRoutes = [
  'DriverHome', 'DriverOrder', 'DriverEarnings', 'DriverProfile'
]

// 需要管理员登录的路由名称列表
const adminAuthRoutes = [
  'AdminDashboard', 'AdminUsers', 'AdminDrivers',
  'AdminOrders', 'AdminAlerts', 'AdminCarTypes',
  'AdminCampaigns', 'AdminVipLevels', 'AdminBusLines',
  'AdminProfile'
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

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
    if (!adminToken || isTokenExpired(adminToken)) {
      clearAllAuth()
      ElMessage.warning('登录已过期，请重新登录')
      return next('/admin/login')
    }
    return next()
  }

  // 司机路由鉴权
  if (driverAuthRoutes.includes(to.name)) {
    if (!driverToken || isTokenExpired(driverToken)) {
      clearAllAuth()
      ElMessage.warning('登录已过期，请重新登录')
      return next('/driver/login')
    }
    return next()
  }

  // 乘客路由鉴权
  if (authRequiredRoutes.includes(to.name)) {
    if (!token || isTokenExpired(token)) {
      clearAllAuth()
      ElMessage.warning('登录已过期，请重新登录')
      return next('/login')
    }
    return next()
  }

  next()
})

export default router
