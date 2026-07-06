# 测试规范补充（项目级）

> 本文件补充 `testing.md`（AUTO-GENERATED，不可编辑）中未覆盖的项目级测试规范。
> 优先级：P1（与 `testing.md` 同级，冲突时本文件优先）。

---

## 1. 测试随机性控制

### 问题背景

项目曾出现以下因随机性导致的测试不稳定：
1. `PaymentServiceImpl` 硬编码 10% 模拟失败率，导致 `testPay` 10% 概率随机失败
2. `VipServiceTest` 使用 `System.currentTimeMillis() % 100` 生成 VIP level，范围 10-109，多次运行后碰撞残留数据
3. 测试数据未清理，跨运行累积导致 UNIQUE 约束冲突

### 规范

**规则 1：生产代码中的随机逻辑必须可配置**

模拟支付成功率、模拟延迟、A/B 测试比例等随机逻辑，必须通过 `@Value` 注入，默认值保留生产语义，测试环境通过 `properties` 覆盖。

```java
// GOOD
@Value("${payment.mock.success-rate:90}")
private int mockSuccessRate;

// BAD — 硬编码，测试无法控制
boolean paySuccess = new Random().nextInt(100) < 90;
```

测试类通过 `@SpringBootTest(properties = "...")` 覆盖：

```java
@SpringBootTest(properties = {"jiangnan.websocket.enabled=false", "payment.mock.success-rate=100"})
class PaymentServiceTest { ... }
```

**规则 2：测试数据标识必须避免碰撞**

使用 `System.currentTimeMillis()` 生成唯一标识时，取模范围必须足够大（≥ 9000），避免跨运行碰撞。

```java
// GOOD — 范围 1000-9999，碰撞概率极低
private static final int VIP_LEVEL = (int)(System.currentTimeMillis() % 9000 + 1000);

// BAD — 范围 10-109，多次运行必碰撞
private static final int VIP_LEVEL = (int)(System.currentTimeMillis() % 100 + 10);
```

**规则 3：测试前防御性清理**

涉及 UNIQUE 约束的测试数据，在创建前先清理可能残留的同 key 记录：

```java
@Test
@Order(1)
void testCreateVipLevel() {
    // 防御性清理
    vipLevelMapper.delete(new LambdaQueryWrapper<VipLevel>()
            .eq(VipLevel::getLevel, VIP_LEVEL));
    // ... 创建逻辑
}
```

---

## 2. 集成测试上下文配置

### 规则

**规则 4：禁用非必要的外部依赖**

集成测试默认禁用 WebSocket、定时任务等非必要依赖，加速上下文加载：

```java
@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
class XxxServiceTest { ... }
```

**规则 5：测试环境变量必须显式注入**

CI 和本地运行测试时，必须显式设置以下环境变量（不得依赖默认值）：

```
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
REDIS_HOST, REDIS_PORT
JWT_SECRET  (至少 256 bits，用于 HS256)
```

---

## 3. 测试套件完整性

### 规则

**规则 6：禁止用 `continue-on-error` 掩盖测试失败**

CI 中测试步骤不得设置 `continue-on-error: true`。测试失败必须立即暴露，不得掩盖。

**规则 7：ApplicationContext 加载失败视为 P0**

任何导致 Spring ApplicationContext 加载失败的 bug（如 bean 冲突、缺失字段）视为 P0 级阻塞，必须立即修复。此类 bug 会导致整个集成测试套件失效。

---

## 4. 与 testing.md 的关系

| 场景 | 适用规则 |
|---|---|
| 测试框架、TDD、AAA 模式、覆盖率 | `testing.md` |
| 随机性控制、上下文配置、测试套件完整性 | 本文件 |
| 冲突时 | 本文件优先 |
