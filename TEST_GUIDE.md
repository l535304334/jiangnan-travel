# 江南出行测试脚本使用说明

## 一、环境要求

- Node.js >= 18（推荐 22.x）
- MySQL 8.0 已启动，数据库 `smart_travel` 已初始化
- 后端服务运行在 `http://localhost:8080`
- 前端服务运行在 `http://localhost:5173`（浏览器测试时需要）

## 二、脚本清单

| 脚本 | 类型 | 说明 | 依赖 |
|---|---|---|---|
| `auth_boundary_test.mjs` | 边界/异常 | 登录错误、无 Token、跨角色访问 | 无 |
| `order_flow_test.mjs` | 核心流程 | 下单、预估价格、优惠券抵扣 | 无 |
| `driver_flow_test.mjs` | 核心流程 | 司机接单、到达、开始、完成行程 | order_flow 创建待接订单 |
| `payment_review_test.mjs` | 核心流程 | 支付、发票、评价 | driver_flow 完成订单 |
| `comprehensive_api_test.mjs` | 回归 | 36 个接口基线覆盖 | 无 |
| `cancel_refund_test.mjs` | 业务专项 | 订单取消、取消率风控 | 无 |
| `notification_test.mjs` | 业务专项 | 通知列表、未读数、标记已读 | 无 |
| `address_test.mjs` | 业务专项 | 地址增删改查 | 无 |
| `coupon_lifecycle_test.mjs` | 业务专项 | 优惠券领取、使用、过期 | 无 |
| `vip_test.mjs` | 业务专项 | VIP 开通、权益、过期 | 无 |
| `bus_line_test.mjs` | 业务专项 | 班线列表、购票 | 无 |
| `idor_security_test.mjs` | 安全 | 越权访问（IDOR） | 无 |
| `jwt_security_test.mjs` | 安全 | Token 伪造、篡改、过期 | 无 |
| `payment_security_test.mjs` | 安全 | 重复支付、回调幂等 | 无 |
| `input_security_test.mjs` | 安全 | SQL 注入、XSS 尝试 | 无 |

## 三、快速执行

### 3.1 全量测试套件

```bash
node test-suite.mjs
```

输出示例：

```text
=== 江南出行全量测试套件 ===
模式：完整测试 | 清理数据：否

## 核心链路回归
✅ auth_boundary_test.mjs | 12/12 通过 | 0 失败 | 1200ms
...

=== 全量测试汇总 ===
总用例：...
通过：...
失败：...
通过率：...
```

### 3.2 跳过安全测试

```bash
node test-suite.mjs --skip-security
```

### 3.3 标记清理意图（不自动执行 SQL）

```bash
node test-suite.mjs --cleanup
```

## 四、可重复执行保障

1. **司机状态补偿**：`driver_flow_test.mjs`、`idor_security_test.mjs`、`payment_security_test.mjs` 在登录后会先把测试司机重置为在线，避免前一个测试中断导致司机处于忙碌状态。
2. **幂等键**：所有下单接口均使用 `idempotentKey`，防止重复提交。
3. **限流保护**：`test-suite.mjs` 在每个脚本之间停顿 1.5 秒，降低触发 9002 限流的概率。
4. **测试数据清理**：运行结束后可手动执行 `cleanup_test_data.sql`，将测试司机重置为在线并删除测试产生的订单、支付、发票、评价、地址等数据。

## 五、数据清理

```bash
mysql -u root -p smart_travel < cleanup_test_data.sql
```

> ⚠️ 仅用于测试/开发环境，生产环境请勿执行。

## 六、常见问题

| 现象 | 原因 | 处理 |
|---|---|---|
| 返回 `code:9002` | 触发限流（每分钟 60 次） | 等待 1 分钟后重试，或降低并发 |
| 司机流程返回 `3003 司机已下线` | 司机状态被前一个测试改为忙碌 | 运行 `cleanup_test_data.sql` 或等待补偿逻辑生效 |
| 支付安全测试失败 | 无已完成未支付订单 | 先运行 `driver_flow_test.mjs` 和 `payment_review_test.mjs` |
| 发票 IDOR 测试跳过 | 该订单已申请过发票 | 正常现象，运行清理脚本后恢复 |

## 七、扩展测试

新增测试脚本时，请遵循以下约定：

1. 文件命名：`{模块}_{场景}_test.mjs`
2. 输出格式：结尾必须打印 `总计: N | 通过: M | 失败: K`
3. 失败时：`process.exit(1)`
4. 在 `test-suite.mjs` 的对应分组中添加脚本路径
