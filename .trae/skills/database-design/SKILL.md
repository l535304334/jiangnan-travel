---
name: database-design
description: 专业数据库设计技能，适用于 MySQL + MyBatis-Plus + Spring Boot 项目。在创建新表、修改表结构、优化查询或设计数据模型时自动触发。涵盖 ER 设计、索引策略、SQL 优化、MyBatis-Plus 代码生成等。
---

# Database Design Skill

## Overview
为 Spring Boot + MyBatis-Plus + MySQL 项目提供专业的数据库设计方案。遵循数据库设计规范，确保性能、可维护性和数据一致性。

## When to Use
- 设计新数据库表结构时
- 需要优化慢查询或索引时
- 修改现有表结构时
- 设计关联关系和事务边界时

## Design Principles

### 1. 命名规范
- 表名: `snake_case` 复数形式 (如 `sys_user`, `order_info`)
- 字段名: `snake_case` (如 `create_time`, `user_name`)
- 主键: `id` BIGINT/LONG 自增
- 外键: `关联表名_id` (如 `user_id`, `order_id`)
- 索引名: `idx_字段名` (普通索引), `uk_字段名` (唯一索引)

### 2. 字段规范
- 必须包含: `id`, `create_time`, `update_time`, `deleted` (逻辑删除)
- 时间字段: `create_time`/`update_time` 使用 `datetime` 类型
- 逻辑删除: `deleted` TINYINT(1) DEFAULT 0
- 金额字段: 使用 `decimal(10,2)` 类型
- 状态字段: 使用 `tinyint` 类型，配合注释说明枚举值

### 3. MyBatis-Plus 映射
- 实体类继承 `BaseEntity` (包含 id, createTime, updateTime, deleted)
- 使用 `@TableName` 和 `@TableId` 注解
- 逻辑删除使用 `@TableLogic`
- 乐观锁使用 `@Version`
- 自动填充使用 `MetaObjectHandler`

### 4. 索引策略
- 主键索引: 默认自增
- 唯一索引: 业务唯一标识
- 普通索引: 查询频繁字段
- 联合索引: 多条件查询，遵循最左前缀原则
- 避免冗余索引

### 5. SQL 优化
- 避免 SELECT *，只查询需要字段
- 合理使用分页 (PageHelper/MyBatis-Plus Page)
- 复杂查询使用 MyBatis-Plus Wrapper 或 XML
- 大批量操作使用批量插入/更新
- 避免在循环中查数据库 (N+1 问题)

## Process
1. 分析需求，识别实体和关系
2. 设计 ER 图 (实体、属性、关系)
3. 设计表结构 (字段、类型、约束)
4. 定义索引策略
5. 编写 DDL 语句
6. 创建对应实体类/Mapper/Service
7. 审核设计 (范式化、性能评估)
