# API 全量测试失败修复计划

## Context

对江南出行后端执行全量 API 测试（`comprehensive_api_test.mjs`）后，36 个端点中 12 个失败，通过率 66.7%。失败原因集中在四类：

1. **重复 `/api` 前缀**：`AIDataController` 类级路径为 `/api`，方法级路径又写 `/api/xxx`，导致实际路径为 `/api/api/xxx`。
2. **权限配置与架构文档不一致**：`SecurityConfig` 将 `ARCHITECTURE.md` 中标注为“公开”的接口配置为需认证。
3. **端点路径与文档不一致**：`/api/bus/lines`、VIP 管理路径等。
4. **参数语义错误/缺失接口**：司机状态更新使用 `@RequestParam` 而调用方传 JSON body；`/api/driver/order/nearby` 逻辑错误；缺少 `/api/admin/bus-lines` 和 `/api/vip/benefits`。

本计划旨在对齐代码与 `ARCHITECTURE.md`，使 36 个被测端点全部通过，且不破坏既有业务。

## Recommended Approach

### 1. 修正公共接口权限（SecurityConfig）

在角色路由之前显式放行 `ARCHITECTURE.md` 中的公开端点：

- `/api/coupon/list`
- `/api/campaign/list`、`/api/campaign/{id}`、`/api/campaign/available-coupons`
- `/api/vip/levels`、`/api/vip/benefits`
- `/api/order/estimate`
- `/api/bus-line/list`、`/api/bus-line/{id}`、`/api/driver/order/nearby`

同时修正 `/api/bus/purchase` 为 `/api/bus-line/purchase`，其余 `/api/coupon/**`、`/api/vip/**`、`/api/order/**` 仍保持需认证。

### 2. 去掉重复 `/api` 前缀（AIDataController）

类上已有 `@RequestMapping("/api")`，方法路径改为相对路径：

- `/api/ai/hotspots` → `/ai/hotspots`
- `/api/common/city-quote` → `/common/city-quote`

### 3. 对齐班线路径（BusLineController）

- 类路径 `/api/bus` → `/api/bus-line`
- `/lines` → `/list`
- `/lines/{id}` → `/{id}`
- `/purchase` 保持 `/purchase`

并新增 admin 班线管理接口：

- `GET /api/admin/bus-lines`
- `POST /api/admin/bus-lines/create`
- `PUT /api/admin/bus-lines/{id}`
- `DELETE /api/admin/bus-lines/{id}`

复用 `BusLineService` + `BusLineMapper`，直接操作 `BusLine` 实体。

### 4. 对齐 Admin VIP 路径（AdminManageController）

将 `/api/admin/vip/levels/*` 改为 `/api/admin/vip-levels/*`：

- `GET /api/admin/vip-levels`
- `POST /api/admin/vip-levels/create`
- `PUT /api/admin/vip-levels/{id}`
- `DELETE /api/admin/vip-levels/{id}`

### 5. 补充 VIP 权益接口（VipController）

新增：

- `GET /api/vip/benefits` → 返回 `vipService.listLevels()`

### 6. 司机状态接口支持 JSON Body（DriverController）

新增 DTO `DriverStatusUpdateRequest`（字段 `Integer status`），`updateStatus` 方法改为 `@RequestBody`。

### 7. 重写附近订单接口（DriverOrderController + OrderService）

`/api/driver/order/nearby` 改为接收 `lat`、`lng`、`limit` 查询参数，查询 `status = 0` 的订单，按距离排序返回 `OrderVO` 列表。

实现：

- 在 `OrderService` / `OrderServiceImpl` 新增 `List<OrderVO> findNearbyOrders(BigDecimal lat, BigDecimal lng, Integer limit)`。
- 用经纬度范围框初筛，再计算欧氏距离近似排序（复用 `DispatchServiceImpl` 中的距离换算逻辑）。
- `DriverOrderController.nearby` 改为 `@RequestParam BigDecimal lat, @RequestParam BigDecimal lng, @RequestParam(defaultValue = "20") int limit`。

## Critical Files to Modify

- `jiangnan-travel/src/main/java/com/jiangnan/travel/security/SecurityConfig.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AIDataController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/BusLineController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminManageController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/VipController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverOrderController.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/service/OrderService.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/OrderServiceImpl.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/service/BusLineService.java`
- `jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/BusLineServiceImpl.java`
- 新增 DTO：`jiangnan-travel/src/main/java/com/jiangnan/travel/dto/DriverStatusUpdateRequest.java`

## Verification

1. 后端热重载（`mvn spring-boot:run` 已启用 devtools 或重启）。
2. 重新执行 `node comprehensive_api_test.mjs`，确认 36 个端点全部通过（状态 200 且 `code === 200`）。
3. 使用 `integrated_browser` MCP 进行前端关键路径回归：
   - 游客访问首页、文旅地标、优惠券中心、活动中心、VIP 中心、班线列表。
   - 乘客登录后下单、查看订单、个人中心。
   - 司机登录后查看附近订单、更新状态。
   - 管理员登录后访问 dashboard、VIP 等级管理、班线管理。
