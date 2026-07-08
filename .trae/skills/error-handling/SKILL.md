---
name: error-handling
description: Spring Boot 统一异常处理模式。在编写涉及异常处理、错误码设计、全局异常拦截、降级策略的后端代码时自动触发。确保所有错误都被显式处理，不吞掉异常。
metadata:
  triggers: 异常处理, 错误码, 错误处理, 全局异常, Exception, 降级, fallback
  scope: implementation
  related-skills: java-architect, code-review-and-quality, rest-api-design
---

# 统一异常处理

Spring Boot 项目统一异常处理模式，确保所有错误被正确捕获、记录和返回。

## 核心工作流

1. **定义错误码枚举** — 按模块分类，使用数字编码
2. **创建业务异常类** — 携带错误码和消息
3. **配置全局异常处理器** — 使用 `@RestControllerAdvice`
4. **Service 层抛出业务异常** — 不吞掉异常，不返回 null
5. **Controller 层只做路由** — 异常由全局处理器统一拦截

## 错误码设计

```java
public enum ErrorCode {
    // 通用错误 (1000-1999)
    SUCCESS(0, "操作成功"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1004, "资源不存在"),
    INTERNAL_ERROR(1500, "系统内部错误"),

    // 用户模块 (2000-2999)
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_PASSWORD_ERROR(2002, "密码错误"),
    USER_TOKEN_EXPIRED(2003, "登录已过期"),
    USER_UNAUTHORIZED(2004, "无操作权限"),

    // 订单模块 (3000-3999)
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态不允许此操作"),
    ORDER_CANCEL_FAILED(3003, "订单取消失败"),

    // 调度模块 (4000-4999)
    DISPATCH_NO_DRIVER(4001, "暂无可用司机"),
    DISPATCH_TIMEOUT(4002, "调度超时");
}
```

## 业务异常类

```java
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.code = errorCode.getCode();
        this.message = detail;
    }
}
```

## 全局异常处理器

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    // 认证异常
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(ErrorCode.USER_UNAUTHORIZED.getCode(), "认证失败");
    }

    // 未知异常（兜底）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknownException(Exception e) {
        log.error("系统异常", e);
        // 不返回堆栈信息给前端
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
```

## 统一响应格式

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
```

## Service 层异常处理模式

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Override
    public OrderVO findById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            // 抛出明确异常，不返回 null
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return OrderConverter.toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.canCancel()) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                "当前状态: " + order.getStatus().getDescription());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);
        return OrderConverter.toVO(order);
    }
}
```

## 降级策略

```java
// Redis 不可用时的降级
public UserVO findByIdWithCache(Long id) {
    try {
        String cached = redisTemplate.opsForValue().get("user:" + id);
        if (cached != null) {
            return JSON.parseObject(cached, UserVO.class);
        }
    } catch (Exception e) {
        log.warn("Redis 查询失败，降级到数据库查询", e);
        // 不抛出异常，降级到数据库
    }
    // 兜底：直接查数据库
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    return UserConverter.toVO(user);
}
```

## 约束

### MUST DO
- 所有 Service 方法异常必须显式抛出（不吞掉、不返回 null 表示错误）
- 使用 `ErrorCode` 枚举统一管理错误码
- 使用 `@RestControllerAdvice` 全局拦截异常
- 日志记录异常详情（`log.warn` 业务异常，`log.error` 系统异常）
- 对外返回用户友好的错误消息，不暴露堆栈
- 批处理跳过时，跳过数量和原因必须在输出中展示

### MUST NOT DO
- 吞掉异常（空 catch 块）
- 返回 null 表示"未找到"（应抛异常）
- 在 Controller 中 try-catch 业务异常（交给全局处理器）
- 向用户暴露堆栈信息或内部错误详情
- 使用 `System.out.println` 打印错误（使用 `@Slf4j` 日志）

## 错误响应示例

```json
// 业务异常响应
{
  "code": 3001,
  "message": "订单不存在",
  "data": null
}

// 参数校验失败响应
{
  "code": 1001,
  "message": "username: 不能为空; email: 格式不正确",
  "data": null
}

// 成功响应
{
  "code": 0,
  "message": "操作成功",
  "data": { "id": 1, "status": "CANCELLED" }
}
```

## 验证

- [ ] 所有 ErrorCode 枚举值不重复
- [ ] `@RestControllerAdvice` 覆盖所有异常类型
- [ ] 错误响应格式统一（code + message + data）
- [ ] 无吞掉的异常（空 catch 块）
- [ ] 无向用户暴露的堆栈信息
- [ ] 日志级别正确（业务异常 warn，系统异常 error）
