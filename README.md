# QuotaFlow

一套面向限量商品、活动名额、优惠券等场景的高并发库存预占服务。项目重点不是堆接口，而是用一个完整业务闭环展示中小型团队常见的后端工程能力。

## 项目亮点

- **防超卖**：使用数据库条件更新 `available_stock >= quantity`，把校验和扣减合并为单条原子 SQL。
- **接口幂等**：`X-Idempotency-Key` + 数据库唯一索引，重试不会重复扣库存。
- **状态机**：预占单支持 `PENDING → CONFIRMED / CANCELLED / EXPIRED`，非法状态迁移被拒绝。
- **库存自动释放**：定时扫描超时预占单，加悲观锁后恢复库存，避免并发重复释放。
- **事务消息**：业务数据和 Outbox 事件在同一事务提交，后台批量发布，避免“业务成功但消息丢失”。
- **生产可观测性**：请求 ID、结构化错误、Actuator 健康检查/指标、OpenAPI 文档。
- **工程化**：Flyway 版本化建表、H2 快速体验、MySQL 生产配置、Docker Compose、GitHub Actions、并发测试。

## 架构

```text
Client → REST API → Rate Limit → Reservation Service
                                  ├─ atomic stock update → Product
                                  ├─ state machine       → Reservation
                                  └─ same transaction    → Outbox Event
                                                        ↓
                                               scheduled publisher
```

## 技术栈

Java 21 · Spring Boot 3 · Spring Data JPA · MySQL 8 / H2 · Flyway · Springdoc OpenAPI · Actuator · JUnit 5 · Docker · GitHub Actions

## 5 分钟运行

本地仅需 Java 21+ 与 Maven，无需安装数据库：

```bash
mvn spring-boot:run
```

打开 Swagger UI：<http://localhost:8080/swagger-ui.html>

生产化方式（应用 + MySQL）：

```bash
docker compose up --build
```

## 快速体验

```bash
# 创建库存
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"限量机械键盘","stock":100}'

# 预占库存（重复使用同一幂等键会返回同一结果）
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: order-2026-0001" \
  -d '{"productId":1,"customerId":"user-1001","quantity":1}'

# 确认预占
curl -X POST http://localhost:8080/api/v1/reservations/{reservationId}/confirm

# 取消并返还库存
curl -X DELETE http://localhost:8080/api/v1/reservations/{reservationId}
```

## 核心接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/products` | 创建库存商品 |
| GET | `/api/v1/products` | 查询商品列表 |
| POST | `/api/v1/reservations` | 幂等预占库存 |
| GET | `/api/v1/reservations/{id}` | 查询预占单 |
| POST | `/api/v1/reservations/{id}/confirm` | 确认预占 |
| DELETE | `/api/v1/reservations/{id}` | 取消并返还库存 |

## 一致性设计

1. **扣库存**：`UPDATE products SET available_stock = available_stock - ? WHERE id = ? AND available_stock >= ?`。受影响行数为 0 时表示库存不足，因此不会出现负库存。
2. **幂等**：业务层先查询幂等键，数据库唯一索引负责兜底。生产环境可进一步把幂等键扩展为“租户 + 接口 + 请求键”。
3. **状态竞争**：确认、取消、超时释放都通过 `SELECT ... FOR UPDATE` 锁定预占单，保证只有一个状态迁移胜出。
4. **可靠事件**：状态变化与 Outbox 写入共用事务。示例发布器输出日志，实际接入 Kafka/RabbitMQ 时只需替换发布实现。

## 测试

```bash
mvn test
```

`ReservationConcurrencyTest` 会同时发起 30 个请求竞争 10 件库存，并断言成功数恰好为 10、剩余库存为 0。

## 可直接写进简历

> **QuotaFlow 高并发库存预占服务**｜Java 21、Spring Boot、MySQL、JPA、Flyway、Docker  
> 独立设计并实现适用于限量商品/活动名额场景的库存预占服务；采用条件更新原子扣减解决并发超卖，通过幂等键与唯一索引避免重复请求；设计预占状态机及超时库存回补机制，使用行锁处理确认、取消、过期任务的状态竞争；引入 Transactional Outbox 保证业务变更与领域事件一致提交，并完善统一异常、请求链路标识、接口限流、健康检查、OpenAPI 文档、并发测试和容器化部署。

## 面试可讲的取舍

- 为什么选择数据库原子扣减，而不是一开始就上 Redis 分布式锁？
- 幂等键“先查再写”仍可能并发，为什么必须保留唯一索引兜底？
- 定时过期任务多实例部署时如何避免重复处理？当前行锁方案什么时候应升级为分片任务？
- Outbox 解决了什么问题？消费者为什么仍然需要幂等？
- 流量上升后如何演进为 Redis 预扣、消息队列削峰、异步落库？

## 后续演进

- Redis + Lua 预扣库存，Kafka 异步削峰
- Prometheus / Grafana 仪表盘与压测报告
- JWT / RBAC、租户隔离、审计日志
- Testcontainers 覆盖 MySQL 真实行为

## License

MIT
