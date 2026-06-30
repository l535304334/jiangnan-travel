# PROJECT_RULES.md — 项目编码规范

> 项目：江南出行（Jiangnan Travel）  
> 适用版本：1.0.0-SNAPSHOT  
> 生效日期：2026-06-24  
> 约束力：**所有代码必须遵守本规范，AI 生成代码和人工代码同等适用。**

---

## 一、项目目录规范

### 1.1 顶层结构

```
jiangnan-travel/               # 后端 Spring Boot 项目（Maven 多模块预留）
├── src/main/java/             # Java 源代码
├── src/main/resources/        # 配置文件
├── src/test/java/             # 单元测试
└── pom.xml                    # Maven 构建文件

jiangnan-travel-web/           # 前端 Vue 项目
├── src/
│   ├── api/                   # API 请求模块
│   ├── assets/                # 静态资源（样式、图片）
│   ├── components/            # 公共组件
│   ├── router/                # 路由配置
│   ├── stores/                # Pinia 状态管理
│   └── views/                 # 页面视图
├── index.html
├── vite.config.js
└── package.json

docs/                          # 项目文档
└── spec/                      # 功能规格说明
```

### 1.2 禁止的目录和文件

- 禁止将 `node_modules/`、`target/`、`dist/` 提交到 Git
- 禁止提交 `.env` 文件（使用 `.env.example` 替代）
- 禁止提交 IDE 配置文件（`.idea/`、`.vscode/`、`*.iml`）
- 禁止提交 `application-local.yml`、`application-dev.yml` 等含敏感信息的配置

---

## 二、包结构规范

### 2.1 基础包名

```
com.jiangnan.travel
```

### 2.2 包职责

| 包 | 职责 | 文件数参考 | 说明 |
|---|---|---|---|
| `common` | 公共类 | 4 | `Result`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` |
| `config` | 配置类 | 8 | `CorsConfig`, `DeepSeekConfig`, `Knife4jConfig`, `MybatisPlusConfig`, `RateLimitConfig`, `RedisCacheConfig`, `MyMetaObjectHandler`, `TestDataInitializer` |
| `controller` | 控制器 | 22 | RESTful API 入口 |
| `dto` | 请求 DTO | 25 | `XxxRequest` |
| `vo` | 响应 VO | 16 | `XxxVO` |
| `entity` | 数据库实体 | 29 | 每张表一个实体 + `BaseEntity` |
| `mapper` | MyBatis-Plus Mapper | 28 | 每个实体对应一个 Mapper |
| `security` | 安全认证 | 3 | `JwtUtil`, `JwtAuthFilter`, `SecurityConfig` |
| `service` | 业务接口 | 22 | Interface 定义 |
| `service/impl` | 业务实现 | 22 | Impl 实现类 |
| `websocket` | WebSocket | 4 | 实时通信（司机位置/订单追踪/通知/配置） |

### 2.3 包依赖规则

```
controller → dto → service → mapper → entity
                ↓
               vo
```

- Controller 层只能依赖 `dto`、`vo`、`service`，不能直接依赖 `mapper` 或 `entity`
- Service 层可以依赖 `mapper`、`entity`
- 不允许循环依赖（Service A → Service B → Service A）

---

## 三、Controller 规范

### 3.1 类结构

```java
@Tag(name = "资源名管理", description = "资源的增删改查接口")
@RestController
@RequestMapping("/api/资源名")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;

    // 方法区域按功能分组，用空行分隔
}
```

### 3.2 URL 命名

| 操作 | HTTP | URL 模式 | 示例 |
|---|---|---|---|
| 新增 | `POST` | `/api/资源名/create` | `POST /api/order/create` |
| 删除 | `DELETE` | `/api/资源名/{id}` | `DELETE /api/order/1` |
| 更新 | `PUT` | `/api/资源名/update` | `PUT /api/user/profile` |
| 详情 | `GET` | `/api/资源名/{id}` | `GET /api/order/1` |
| 列表 | `GET` | `/api/资源名/list` | `GET /api/order/list` |
| 动作 | `POST` | `/api/资源名/{id}/动作` | `POST /api/order/1/cancel` |

### 3.3 方法规范

```java
@Operation(summary = "获取订单详情", description = "根据订单ID获取完整订单信息")
@GetMapping("/{id}")
public Result<OrderVO> detail(@PathVariable Long id, Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    return Result.ok(orderService.getDetail(id, userId));
}

@Operation(summary = "创建订单")
@PostMapping("/create")
public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest request,
                              Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    return Result.ok(orderService.create(request, userId));
}
```

### 3.4 强制规则

- **必须使用** `@Tag`、`@Operation` 注解（Knife4j/SpringDoc 自动生成文档）
- **必须使用** `@RequiredArgsConstructor` + `private final` 注入，**禁止** `@Autowired`
- **必须返回** `Result<T>` 类型，**禁止**直接返回实体或 `ResponseEntity`
- 接收参数使用 `@Valid` 触发校验
- 获取当前用户通过 `Authentication authentication` 参数注入，**禁止**从 `SecurityContextHolder` 直接获取
- 方法名使用动词（`create`、`cancel`、`list`），**禁止**使用 `add`、`del`、`query` 等非标准命名

---

## 四、Service 规范

### 4.1 接口 + 实现分离

```java
// 接口
public interface OrderService {
    OrderVO create(CreateOrderRequest request, Long userId);
    OrderVO cancel(Long orderId, Long userId);
    PageResult<OrderVO> listByUser(Long userId, int page, int size);
}

// 实现
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    // ...
}
```

### 4.2 方法命名

| 操作 | 方法名 | 说明 |
|---|---|---|
| 新增 | `create()` | 创建资源 |
| 删除 | `deleteById()` | 逻辑删除（设置 `deleted=1`） |
| 更新 | `update()` | 更新资源 |
| 查询单条 | `getById()` 或 `getDetail()` | 返回 VO |
| 查询列表 | `listByXxx()` | 按维度查询 |
| 分页查询 | `pageByXxx()` | 返回分页结果 |

### 4.3 事务规范

- 写操作涉及多表时，必须加 `@Transactional(rollbackFor = Exception.class)`
- 读操作**不加** `@Transactional`
- 事务方法只能在 public 方法上使用，避免自调用导致事务失效

### 4.4 强制规则

- **必须使用** Interface + Impl 模式
- **必须使用** `@RequiredArgsConstructor` + `private final` 注入
- **必须使用** `@Slf4j` 记录关键日志
- 禁止在 Service 层直接处理 HTTP 请求参数或构造 HTTP 响应

---

## 五、Mapper 规范

### 5.1 基本结构

```java
@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {
    // MyBatis-Plus 提供基础 CRUD，一般无需额外方法
}
```

### 5.2 查询方式

**纯 MyBatis-Plus 模式，禁止使用 XML 映射文件。**

```java
// 单条查询
User user = userMapper.selectOne(
    new LambdaQueryWrapper<User>()
        .eq(User::getPhone, phone)
        .eq(User::getDeleted, 0));

// 分页查询
IPage<Order> page = orderMapper.selectPage(
    new Page<>(pageNum, pageSize),
    new LambdaQueryWrapper<Order>()
        .eq(Order::getUserId, userId)
        .eq(Order::getDeleted, 0)
        .orderByDesc(Order::getCreateTime));
```

### 5.3 强制规则

- **必须继承** `BaseMapper<T>`，**禁止**自定义 XML 映射
- **必须使用** `@Mapper` + `@Repository` 双注解
- 使用 `LambdaQueryWrapper` 链式调用，**禁止**字符串硬编码字段名
- 所有查询必须加上逻辑删除条件（`eq(BaseEntity::getDeleted, 0)`）
- 复杂统计查询使用 `@Select` 注解写在 Mapper 接口上

---

## 六、DTO/VO 规范

### 6.1 请求 DTO（XxxRequest）

```java
@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotBlank(message = "起点地址不能为空")
    @Schema(description = "起点地址", example = "无锡火车站")
    private String startAddress;

    @NotNull(message = "起点经度不能为空")
    @Schema(description = "起点经度", example = "120.31191")
    private Double startLng;

    @NotNull(message = "车型ID不能为空")
    @Positive(message = "车型ID必须为正数")
    @Schema(description = "车型ID", example = "1")
    private Long carTypeId;
}
```

### 6.2 响应 VO（XxxVO）

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单信息")
public class OrderVO {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单状态", example = "pending")
    private String status;

    @Schema(description = "预估价格", example = "25.50")
    private BigDecimal estimatedPrice;

    @Schema(description = "司机信息")
    private DriverVO driver;
}
```

### 6.3 强制规则

| 规则 | 说明 |
|---|---|
| 请求 DTO 命名 | `XxxRequest`，放在 `dto` 包 |
| 响应 VO 命名 | `XxxVO`，放在 `vo` 包，**必须使用** `@Builder` |
| 参数校验 | DTO 字段必须加 `@NotBlank` / `@NotNull` / `@Positive` 等约束，`message` 使用中文 |
| Swagger 注解 | 所有字段必须加 `@Schema(description = "...", example = "...")` |
| 复杂嵌套 | VO 可以嵌套其他 VO（如 `OrderVO.driver` 为 `DriverVO` 类型） |
| 禁止暴露实体 | 禁止直接将 Entity 作为请求参数或响应返回 |

---

## 七、Result 返回规范

### 7.1 统一响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 7.2 Result 方法

```java
// 成功（无数据）
Result.ok()

// 成功（带数据）
Result.ok(data)

// 成功（自定义消息 + 数据）
Result.ok(message, data)

// 失败（自定义 code + 消息）
Result.fail(code, message)

// 失败（默认 500）
Result.fail(message)
```

### 7.3 错误码规范（ErrorCode）

```java
@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 模块编号
    // 1000-1999 用户模块
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PHONE_EXISTS(1002, "手机号已注册"),
    USER_CODE_ERROR(1003, "验证码错误"),
    USER_CODE_EXPIRED(1004, "验证码已过期"),

    // 2000-2999 订单模块
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态异常"),
    ORDER_CANNOT_CANCEL(2003, "当前状态不可取消"),

    // 3000-3999 司机模块
    DRIVER_NOT_FOUND(3001, "司机不存在"),
    DRIVER_UNAVAILABLE(3002, "司机当前不可接单"),

    // 4000-4999 风控模块
    RISK_TOO_MANY_CANCELS(4001, "取消次数过多，暂不可下单"),
    RISK_ALERT(4002, "存在安全风险"),

    // 5000-5999 AI 模块
    AI_SERVICE_ERROR(5001, "AI 服务调用失败"),

    // 6000-6999 支付模块
    PAYMENT_FAILED(6001, "支付失败"),
    PAYMENT_TIMEOUT(6002, "支付超时"),

    // 9000-9999 系统模块
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    RATE_LIMIT(9002, "请求过于频繁");
}
```

### 7.4 强制规则

- **所有 Controller 方法必须返回 `Result<T>`**（SSE 流式端点返回 `SseEmitter` 除外）
- 成功返回 `Result.ok(data)`
- 业务错误抛 `BusinessException(ErrorCode.xxx)`，由 `GlobalExceptionHandler` 统一处理
- 新增错误码遵循模块编号规则，**禁止**随意编号

---

## 八、异常处理规范

### 8.1 BusinessException

```java
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### 8.2 GlobalExceptionHandler

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：返回 200，code 由 ErrorCode 定义 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常：收集所有字段错误信息 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return Result.fail(400, msg);
    }

    /** 未预料的异常：不暴露内部详情 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(500, "服务器内部错误，请稍后重试");
    }
}
```

### 8.3 强制规则

- 业务异常**必须**使用 `BusinessException` + `ErrorCode`，**禁止**直接 `throw new RuntimeException()`
- Controller 层**禁止** try-catch，全部交由 `GlobalExceptionHandler` 处理
- 参数校验**禁止**在 Controller 中手动 if 判断，使用 `@Valid` + DTO 约束注解
- 敏感信息（SQL 语句、堆栈信息）**禁止**出现在返回给前端的错误消息中

---

## 九、日志规范

### 9.1 使用方式

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    // 业务关键节点
    public OrderVO create(CreateOrderRequest request, Long userId) {
        log.info("用户[{}]创建订单: {}", userId, request);
        // ...
        log.info("订单[{}]创建成功", order.getId());
        // ...
        log.warn("用户[{}]重复创建订单", userId);
        // ...
        log.error("支付回调处理失败, 订单ID: {}", orderId, e);
    }
}
```

### 9.2 日志级别规范

| 级别 | 使用场景 |
|---|---|
| `trace` | 仅调试用，发布环境关闭 |
| `debug` | 开发调试细节，默认关闭 |
| `info` | 业务关键节点（创建、修改、删除、登录等） |
| `warn` | 非预期但可恢复的情况（重复操作、参数异常等） |
| `error` | 不可恢复的错误（数据库异常、第三方服务调用失败等） |

### 9.3 日志内容规范

- **必须包含**关键业务标识（用户ID、订单ID、手机号等）
- **禁止**记录密码、验证码、Token 等敏感信息
- **禁止**使用 `System.out.println()`，全部使用 Lombok `@Slf4j`
- 异常日志**必须**传入异常对象 `log.error("消息", exception)`，**禁止**只记录 `e.getMessage()`

---

## 十、数据库命名规范

### 10.1 表命名

| 规则 | 示例 |
|---|---|
| 所有表以 `t_` 前缀 | `t_user`, `t_order` |
| 单词间用下划线分隔 | `t_car_type`, `t_user_address`, `t_order_track` |
| 表名使用单数形式 | `t_user`（不是 t_users） |
| 关联表用两表名连接 | `t_user_coupon` |
| 名称不超过 30 个字符 | — |

### 10.2 字段命名

| 规则 | 示例 |
|---|---|
| 全小写下划线 | `car_plate`, `verify_status`, `last_login_time` |
| 主键统一为 `id` | `id BIGINT AUTO_INCREMENT` |
| 外键 = 关联表名（去掉 `t_`）+ `_id` | `user_id`, `order_id`, `driver_id` |
| 逻辑删除字段 | `deleted TINYINT DEFAULT 0` |
| 时间字段 | `create_time`, `update_time` |

### 10.3 字段类型规范

| Java 类型 | MySQL 类型 | 说明 |
|---|---|---|
| `Long` | `bigint` | 主键、外键 |
| `String` | `varchar(N)` | 短文本（N 按实际长度设置，如 phone=varchar(20)） |
| `String` | `text` | 长文本（超过 500 字符） |
| `Integer` | `int` | 整数 |
| `BigDecimal` | `decimal(10,2)` | 金额 |
| `Double` | `double` | 坐标、评分 |
| `LocalDateTime` | `datetime` | 日期时间 |
| `LocalDate` | `date` | 日期 |
| `Boolean` / `Integer` | `tinyint(1)` | 布尔值 |

### 10.4 索引规范

```sql
-- 主键索引（MyBatis-Plus 自动）
PRIMARY KEY (`id`)

-- 唯一索引
UNIQUE INDEX uk_phone (`phone`)

-- 普通索引
INDEX idx_user_id (`user_id`)
INDEX idx_status_create_time (`status`, `create_time`)
```

| 场景 | 索引规则 |
|---|---|
| 唯一约束 | `uk_` 前缀 |
| 普通索引 | `idx_` 前缀 |
| 复合索引 | 选择性高的列在前 |
| 索引数量 | 单表不超过 5 个索引 |

---

## 十一、SQL 编写规范

### 11.1 MyBatis-Plus 查询

```java
// ✔ 正确：LambdaQueryWrapper 链式调用
LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
    .eq(Order::getUserId, userId)
    .eq(Order::getDeleted, 0)
    .orderByDesc(Order::getCreateTime);

// ✘ 错误：字符串硬编码字段名
// new QueryWrapper<Order>().eq("user_id", userId)
```

### 11.2 连接查询

```java
// ✔ 正确：使用 MyBatis-Plus 的 @Select 注解写在 Mapper 上
@Select("SELECT o.*, d.real_name as driverName " +
        "FROM t_order o " +
        "LEFT JOIN t_driver d ON o.driver_id = d.id " +
        "WHERE o.id = #{orderId}")
OrderWithDriverVO getOrderWithDriver(@Param("orderId") Long orderId);
```

### 11.3 强制规则

- **禁止**在 Service 中拼写 SQL 字符串，全部通过 MyBatis-Plus 或注解执行
- 多表查询**必须**使用明确的列名（`o.*` 或 `o.id, o.status`），**禁止** `SELECT *`
- WHERE 条件**必须**包含逻辑删除条件（`deleted = 0`）
- 分页查询**必须**使用 `Page<T>` + `selectPage` 方法
- 大批量操作（1000+ 行）**必须**分批处理

---

## 十二、Git Commit 规范

### 12.1 提交信息格式

```
type: 简短描述

可选：详细说明（为什么要做这个变更，影响了什么）
```

### 12.2 type 类型

| type | 说明 | 示例 |
|---|---|---|
| `feat` | 新功能 | `feat: 新增用户注册短信验证` |
| `fix` | 修复 Bug | `fix: 修复订单取消后状态未更新的问题` |
| `refactor` | 重构 | `refactor: 抽取订单价格计算逻辑到独立类` |
| `perf` | 性能优化 | `perf: 优化订单列表查询，增加索引` |
| `style` | 代码格式 | `style: 格式化 OrderController 代码` |
| `docs` | 文档 | `docs: 更新订单模块 API 文档` |
| `test` | 测试 | `test: 新增用户登录单元测试` |
| `chore` | 构建/配置 | `chore: 升级 MyBatis-Plus 至 3.5.7` |

### 12.3 强制规则

- 提交信息**必须**使用中文
- 提交前**必须**运行测试并确保全部通过
- 一个提交只包含一个功能点，**禁止**混合多个不相关变更
- **禁止**提交包含 `TODO`、`FIXME`、`console.log`、`System.out.println` 的代码
- **禁止**提交未使用的导入和注释掉的代码

---

## 十三、AI 编码规范

### 13.1 生成规则

1. **遵循现有模式**：AI 生成的代码必须与项目中已有的同类代码风格一致。
2. **最小化修改**：只修改实现需求所必需的文件，不波及无关文件。
3. **不生成占位符**：**禁止**生成 `// TODO: 待实现`、`return null;`、`// 后续补充` 等占位代码。
4. **不生成注释代码**：**禁止**生成 `// 以下为旧版本代码` 等注释掉的代码块。
5. **注解完整性**：新 Controller 必须加 `@Tag`、`@Operation`；新 DTO/VO 必须加 `@Schema`。

### 13.2 代码编写优先级

```
1. 先写测试（单元测试/集成测试）
2. 再写测试能通过的最小实现代码
3. 然后重构优化
4. 最后更新文档
```

### 13.3 禁止行为

- ❌ 禁止一次性生成整个项目或整个模块
- ❌ 禁止在需求不明确时猜测实现
- ❌ 禁止生成未使用的 import
- ❌ 禁止使用 `@Autowired` 字段注入
- ❌ 禁止在代码中硬编码环境敏感信息（数据库密码、API Key、密钥等）
- ❌ 禁止跳过 Code Review 直接提交

### 13.4 测试要求

- 业务逻辑类**必须**包含单元测试
- Controller 层**必须**包含接口测试
- 测试**必须**覆盖正常路径和异常路径
- 测试方法命名：`testXxx_when条件_expect期望结果`

---

## 十四、附录

### 14.1 关键配置文件

| 配置 | 位置 |
|---|---|
| 应用配置 | `jiangnan-travel/src/main/resources/application.yml` |
| 数据库 | `spring.datasource.url=jdbc:mysql://localhost:3306/smart_travel` |
| Redis | `spring.data.redis.host=localhost` |
| JWT 密钥 | `jwt.secret`（自定义配置） |
| DeepSeek | AI API Key（自定义配置） |
| 高德地图 | Web API Key + JS API Key（自定义配置） |

### 14.2 开发环境

| 配置 | 值 |
|---|---|
| Java | 17 |
| Node.js | 22.19.0 |
| MySQL | 8.0.42（localhost:3306） |
| Redis | localhost:6379 |
| 后端端口 | 8080 |
| 前端代理 | Vite 代理 `/api` → `localhost:8080` |

### 14.3 本地开发启动

```bash
# 1. 启动 MySQL 和 Redis

# 2. 启动后端
cd jiangnan-travel
mvn spring-boot:run

# 3. 启动前端
cd jiangnan-travel-web
npm install
npm run dev
```

---

> 本规范由 AI 开发环境根据项目实际代码约定自动生成。  
> 所有代码（含 AI 生成代码）默认遵守本规范。  
> 生成时间：2026-06-24
