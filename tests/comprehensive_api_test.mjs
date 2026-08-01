// 江南出行全量 API 功能测试
const BASE = 'http://localhost:8080';

let userToken = null;
let driverToken = null;
let adminToken = null;
let testOrderId = null;
let testUserId = null;

const results = [];

async function request(method, path, body = null, token = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const options = { method, headers };
  if (body) options.body = JSON.stringify(body);
  try {
    const res = await fetch(`${BASE}${path}`, options);
    const text = await res.text();
    let data = null;
    try { data = JSON.parse(text); } catch { data = text; }
    return { status: res.status, data };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

function record(name, path, status, ok) {
  results.push({ name, path, status, ok });
  const mark = ok ? '✅' : '❌';
  console.log(`${mark} ${name} [${path}] -> status:${status}`);
}

async function testPublic() {
  console.log('\n=== 公开接口测试 ===');
  const endpoints = [
    ['GET', '/api/landmark', '文旅地标列表'],
    ['GET', '/api/landmark/search?city=南昌&keyword=滕王阁', '文旅地标搜索'],
    ['GET', '/api/coupon/list', '优惠券列表（文档称公开，实际需认证）'],
    ['GET', '/api/campaign/list', '活动列表'],
    ['GET', '/api/vip/levels', 'VIP等级列表'],
    ['GET', '/api/vip/benefits', 'VIP权益'],
    ['GET', '/api/bus-line/list', '班线列表'],
    ['GET', '/api/common/city-quote', '城市语录'],
    ['POST', '/api/order/estimate', '预估价格', { startAddress: '南昌八一广场', startLat: 28.68, startLng: 115.89, endAddress: '南昌西站', endLat: 28.65, endLng: 115.92, distance: 12000, duration: 1800, carTypeId: 1 }],
    // /api/ai/* 已收紧为需登录（安全加固），移至用户端分组测试
  ];
  for (const ep of endpoints) {
    const [method, path, name, body] = ep;
    const res = await request(method, path, body);
    const ok = res.status === 200 && (typeof res.data === 'object' ? res.data?.code === 200 : true);
    record(name, path, res.status, ok);
  }
}

async function testAuth() {
  console.log('\n=== 认证接口测试 ===');
  const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' });
  record('用户密码登录', '/api/user/login-password', userLogin.status, userLogin.status === 200 && userLogin.data?.code === 200);
  if (userLogin.data?.code === 200) userToken = userLogin.data.data.token;

  const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' });
  record('司机登录', '/api/driver/login', driverLogin.status, driverLogin.status === 200 && driverLogin.data?.code === 200);
  if (driverLogin.data?.code === 200) driverToken = driverLogin.data.data.token;

  const adminLogin = await request('POST', '/api/admin/login', { username: 'admin', password: '123456' });
  record('管理员登录', '/api/admin/login', adminLogin.status, adminLogin.status === 200 && adminLogin.data?.code === 200);
  if (adminLogin.data?.code === 200) adminToken = adminLogin.data.data.token;
}

async function testUser() {
  console.log('\n=== 用户端接口测试 ===');
  if (!userToken) { console.log('跳过：未获取用户Token'); return; }

  const profile = await request('GET', '/api/user/profile', null, userToken);
  record('获取用户资料', '/api/user/profile', profile.status, profile.status === 200 && profile.data?.code === 200);
  if (profile.data?.code === 200) testUserId = profile.data.data.id;

  const address = await request('GET', '/api/user/address', null, userToken);
  record('地址列表', '/api/user/address', address.status, address.status === 200 && address.data?.code === 200);

  const orderList = await request('GET', '/api/order/list', null, userToken);
  record('订单列表', '/api/order/list', orderList.status, orderList.status === 200 && orderList.data?.code === 200);

  if (orderList.data?.data?.records?.length > 0) {
    testOrderId = orderList.data.data.records[0].id;
    const detail = await request('GET', `/api/order/${testOrderId}`, null, userToken);
    record('订单详情', `/api/order/${testOrderId}`, detail.status, detail.status === 200 && detail.data?.code === 200);
  } else {
    console.log('ℹ️ 无历史订单，跳过订单详情/取消测试');
  }

  const myCoupon = await request('GET', '/api/coupon/my', null, userToken);
  record('我的优惠券', '/api/coupon/my', myCoupon.status, myCoupon.status === 200 && myCoupon.data?.code === 200);

  const notifications = await request('GET', '/api/notification/list', null, userToken);
  record('通知列表', '/api/notification/list', notifications.status, notifications.status === 200 && notifications.data?.code === 200);

  const hotspots = await request('GET', '/api/ai/hotspots', null, userToken);
  record('需求热点(需登录)', '/api/ai/hotspots', hotspots.status, hotspots.status === 200 && hotspots.data?.code === 200);

  const aiChat = await request('POST', '/api/ai/chat', { message: '你好', sessionId: null }, userToken);
  record('AI聊天(需登录)', '/api/ai/chat', aiChat.status, aiChat.status === 200 && aiChat.data?.code === 200);

  const unread = await request('GET', '/api/notification/unread-count', null, userToken);
  record('未读通知数', '/api/notification/unread-count', unread.status, unread.status === 200 && unread.data?.code === 200);

  const vipMy = await request('GET', '/api/vip/my', null, userToken);
  record('我的VIP', '/api/vip/my', vipMy.status, vipMy.status === 200 && vipMy.data?.code === 200);

  const invoiceList = await request('GET', '/api/invoice/list', null, userToken);
  record('发票列表', '/api/invoice/list', invoiceList.status, invoiceList.status === 200 && invoiceList.data?.code === 200);
}

async function testDriver() {
  console.log('\n=== 司机端接口测试 ===');
  if (!driverToken) { console.log('跳过：未获取司机Token'); return; }

  // 前置补偿：确保司机在线
  await request('PUT', '/api/driver/status', { status: 1 }, driverToken);

  const profile = await request('GET', '/api/driver/profile', null, driverToken);
  record('司机资料', '/api/driver/profile', profile.status, profile.status === 200 && profile.data?.code === 200);

  const location = await request('PUT', '/api/driver/location', { lat: 28.68, lng: 115.89 }, driverToken);
  record('更新位置', '/api/driver/location', location.status, location.status === 200 && location.data?.code === 200);

  const status = await request('PUT', '/api/driver/status', { status: 1 }, driverToken);
  record('更新状态', '/api/driver/status', status.status, status.status === 200 && status.data?.code === 200);

  const earning = await request('GET', '/api/driver/earning', null, driverToken);
  record('收入统计', '/api/driver/earning', earning.status, earning.status === 200 && earning.data?.code === 200);

  const nearby = await request('GET', '/api/driver/order/nearby?lat=28.68&lng=115.89', null, driverToken);
  record('附近订单', '/api/driver/order/nearby', nearby.status, nearby.status === 200 && nearby.data?.code === 200);
}

async function testAdmin() {
  console.log('\n=== 管理后台接口测试 ===');
  if (!adminToken) { console.log('跳过：未获取管理员Token'); return; }

  const dashboard = await request('GET', '/api/admin/dashboard', null, adminToken);
  record('数据大屏', '/api/admin/dashboard', dashboard.status, dashboard.status === 200 && dashboard.data?.code === 200);

  const users = await request('GET', '/api/admin/users', null, adminToken);
  record('用户列表', '/api/admin/users', users.status, users.status === 200 && users.data?.code === 200);

  const drivers = await request('GET', '/api/admin/drivers', null, adminToken);
  record('司机列表', '/api/admin/drivers', drivers.status, drivers.status === 200 && drivers.data?.code === 200);

  const orders = await request('GET', '/api/admin/orders', null, adminToken);
  record('订单列表', '/api/admin/orders', orders.status, orders.status === 200 && orders.data?.code === 200);

  const alerts = await request('GET', '/api/admin/alerts', null, adminToken);
  record('告警列表', '/api/admin/alerts', alerts.status, alerts.status === 200 && alerts.data?.code === 200);

  const carTypes = await request('GET', '/api/admin/car-types', null, adminToken);
  record('车型列表', '/api/admin/car-types', carTypes.status, carTypes.status === 200 && carTypes.data?.code === 200);

  const campaigns = await request('GET', '/api/admin/campaigns', null, adminToken);
  record('活动管理列表', '/api/admin/campaigns', campaigns.status, campaigns.status === 200 && campaigns.data?.code === 200);

  const vipLevels = await request('GET', '/api/admin/vip-levels', null, adminToken);
  record('VIP等级管理', '/api/admin/vip-levels', vipLevels.status, vipLevels.status === 200 && vipLevels.data?.code === 200);

  const busLines = await request('GET', '/api/admin/bus-lines', null, adminToken);
  record('班线管理列表', '/api/admin/bus-lines', busLines.status, busLines.status === 200 && busLines.data?.code === 200);
}

async function printSummary() {
  console.log('\n=== 测试结果汇总 ===');
  const passed = results.filter(r => r.ok).length;
  const failed = results.filter(r => !r.ok).length;
  console.log(`总计: ${results.length} | 通过: ${passed} | 失败: ${failed} | 通过率: ${(passed/results.length*100).toFixed(1)}%`);
  if (failed > 0) {
    console.log('\n失败项：');
    results.filter(r => !r.ok).forEach(r => console.log(`  ❌ ${r.name} [${r.path}] status:${r.status}`));
  }
}

(async () => {
  await testPublic();
  await testAuth();
  await testUser();
  await testDriver();
  await testAdmin();
  await printSummary();
})();
