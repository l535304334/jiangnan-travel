// API 验证测试
async function testAPI() {
  const BASE = 'http://localhost:8080';

  // 1. 测试地标接口
  console.log('\n=== 1. 文旅地标接口 ===');
  try {
    const res = await fetch(`${BASE}/api/landmark`);
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code);
    console.log('地标数量:', data.data?.length);
    if (data.data?.[0]) {
      console.log('第一个地标:', data.data[0].name, '-', data.data[0].city);
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  // 2. 测试优惠券接口
  console.log('\n=== 2. 优惠券接口 ===');
  try {
    const res = await fetch(`${BASE}/api/coupon/list`);
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code);
    console.log('优惠券数量:', data.data?.length);
    if (data.data?.[0]) {
      console.log('第一个优惠券:', data.data[0].name);
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  // 3. 测试AI聊天接口（无需认证）
  console.log('\n=== 3. AI聊天接口 ===');
  try {
    const res = await fetch(`${BASE}/api/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: '你好', sessionId: null })
    });
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code);
    if (data.code === 200) {
      console.log('AI回复:', data.data?.reply?.substring(0, 80) + '...');
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  // 4. 测试司机登录
  console.log('\n=== 4. 司机登录接口 ===');
  try {
    const res = await fetch(`${BASE}/api/driver/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: '13810000001' })
    });
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code, '| Message:', data.message);
    if (data.code === 200) {
      console.log('司机登录成功, token:', data.data?.token?.substring(0, 20) + '...');
    } else {
      console.log('司机登录失败, 错误:', data.message);
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  // 5. 测试管理员登录
  console.log('\n=== 5. 管理员登录接口 ===');
  try {
    const res = await fetch(`${BASE}/api/admin/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: 'admin123' })
    });
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code, '| Message:', data.message);
    if (data.code === 200) {
      console.log('管理员登录成功, token:', data.data?.token?.substring(0, 20) + '...');
    } else {
      console.log('管理员登录失败, 错误:', data.message);
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  // 6. 测试用户登录
  console.log('\n=== 6. 用户密码登录 ===');
  try {
    const res = await fetch(`${BASE}/api/user/login-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: '13900001111', password: '123456' })
    });
    const data = await res.json();
    console.log('Status:', res.status, '| Code:', data.code, '| Message:', data.message);
    if (data.code === 200) {
      console.log('用户登录成功, token:', data.data?.token?.substring(0, 20) + '...');
    } else {
      console.log('用户登录失败, 错误:', data.message);
    }
  } catch (e) {
    console.error('FAIL:', e.message);
  }

  console.log('\n=== 测试完毕 ===');
}

testAPI().catch(console.error);
