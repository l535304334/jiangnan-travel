---
name: code-refactoring
description: Java/Spring Boot 代码重构技能，在需要改进现有代码结构、性能或可维护性时自动触发。涵盖 SOLID 原则、设计模式应用、代码异味消除、性能优化等。
---

# Code Refactoring Skill

## Overview
为 Java + Spring Boot 项目提供系统化的代码重构方案。在保持外部行为不变的前提下改善代码内部结构。

## When to Use
- 代码存在重复逻辑 (DRY 违规)
- 方法/类过于庞大 (违反单一职责)
- 耦合度过高，内聚性不足
- 存在性能瓶颈
- 需要引入设计模式解决问题
- 遗留代码现代化改造

## Refactoring Principles

### 1. SOLID 原则
- **S**ingle Responsibility: 每个类/方法只负责一个职责
- **O**pen/Closed: 对扩展开放，对修改关闭
- **L**iskov Substitution: 子类可替换父类
- **I**nterface Segregation: 接口粒度适中
- **D**ependency Inversion: 依赖抽象而非具体实现

### 2. 常见重构手法
| 手法 | 适用场景 |
|------|---------|
| 提取方法 (Extract Method) | 长方法、重复代码块 |
| 提取类 (Extract Class) | 类职责过多 |
| 引入接口 (Extract Interface) | 多个类有共同行为 |
| 重命名 (Rename) | 命名不够清晰 |
| 移动方法 (Move Method) | 方法放在错误类中 |
| 参数对象 (Introduce Parameter Object) | 过多参数 |
| 使用设计模式 (Replace with Design Pattern) | 条件逻辑复杂 |
| 拆分循环 (Split Loop) | 循环中做多件事 |

### 3. Spring Boot 重构重点
- Controller: 只做参数校验和路由，业务逻辑移到 Service
- Service: 业务逻辑层，事务管理，调用 Mapper
- Mapper: 纯数据访问，复杂查询用 XML
- DTO/VO: 明确区分请求和响应对象
- 配置: 外部化配置，使用 @ConfigurationProperties

### 4. 性能重构
- 识别 N+1 查询，使用 JOIN FETCH 或 Batch 策略
- 添加合理缓存 (@Cacheable)
- 优化循环内的数据库调用
- 使用批量操作代替逐条操作
- 避免在事务中做远程调用

### 5. 代码异味检测
- 过长方法 (> 50 行)
- 过大的类 (> 500 行)
- 过多参数 (> 4 个)
- 循环复杂度高
- 硬编码值
- 异常处理不当 (吞异常、catch Exception)
- 空指针风险 (未做 null 检查)

## Process
1. 分析现有代码，识别问题
2. 制定重构计划 (列出改动点和影响范围)
3. 编写测试用例确保行为不变
4. 逐步重构 (每次只改一个点)
5. 运行测试验证
6. Code Review 确认
