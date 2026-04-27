# BPM消息中间件模块

## 功能概述

本模块提供了与BPM系统集成的消息中间件功能，包括：

- 消息发布：向BPM系统发送业务消息
- 消息订阅：接收和处理BPM系统的业务消息
- 自动配置：基于注解的零配置集成

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jasolar.mis.framework</groupId>
    <artifactId>jasolar-spring-boot-starter-biz-bpm</artifactId>
    <version>${revision}</version>
</dependency>
```

### 2. 配置参数

```yaml
fiifoxconn:
  bpm:
    enabled: true                            # 启用BPM消息模块
    publisher-enabled: true                  # 启用消息发布功能
    consumer-enabled: true                   # 启用消息消费功能
    current-module: your-service-name        # 当前服务模块名
    exchange-name: BPM.TOPIC                 # 交换机名称
    queue-prefix: BPM                        # 队列名前缀
    routing-key-prefix: BPM                  # 路由键前缀
    # 配置消费者（可选，也可通过注解配置）
    consumers:
      - subModule: order                     # 子模块名
        routingKeys: order.created,order.updated # 路由键列表
        concurrency: 3                       # 并发消费者数
        manualAck: true                      # 手动确认
        requeueOnFail: true                  # 失败重新入队
        maxRetries: 3                        # 最大重试次数
```

### 3. 定义消息处理器

推荐使用**注解方式**定义消息处理器：

```java
import com.jasolar.mis.framework.bpm.annotation.BpmMessageListener;
import com.jasolar.mis.framework.bpm.handler.BpmMessageHandler;
import com.jasolar.mis.framework.bpm.mq.context.BpmMessageContext;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;

@BpmMessageListener(
        subModule = "order",                    // 子模块名称
        routingKeys = "order.created",          // 路由键
        concurrency = 3,                        // 并发消费者数
        maxRetries = 5                          // 最大重试次数
)
public class OrderMessageHandler implements BpmMessageHandler {

    @Override
    public void handleMessage(com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO message, BpmMessageContext context) throws IOException {
        try {
            // 处理消息逻辑
            OrderDTO orderData = (OrderDTO) message.getData();
            orderService.processOrder(orderData);

            // 成功处理，确认消息
            context.ack();
        } catch (Exception e) {
            // 处理失败，拒绝消息
            context.nack(true); // true表示重新入队
        }
    }
}
```

### 4. 发送消息

```java
import com.jasolar.mis.framework.bpm.mq.publisher.BpmMessagePublisher;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;

@Service
public class OrderService {

    @Autowired
    private BpmMessagePublisher bpmMessagePublisher;

    public void createOrder(OrderDTO order) {
        // 业务逻辑...

        // 发送消息
        BaseBpmMessageDTO message = new BaseBpmMessageDTO();
        message.setType("ORDER_CREATED");
        message.setData(order);

        bpmMessagePublisher.send("order.created", message);
    }
}
```

## 高级特性

### 注解配置与YAML配置优先级

当注解配置和YAML配置同时存在时，框架会按以下规则处理：

1. 如果队列名匹配，则合并配置
2. 如果子模块名匹配，则合并配置
3. 注解中的非默认值优先于YAML中的默认值
4. YAML中的显式配置优先于注解中的默认值

### 消息确认模式

框架支持自动确认和手动确认两种模式：

```java
// 自动确认模式
@BpmMessageListener(
    subModule = "order",
    manualAck = false
)
public class AutoAckHandler implements BpmMessageHandler {
    @Override
    public void handleMessage(BaseBpmMessageDTO message) {
        // 处理完自动确认
    }
}

// 手动确认模式
@BpmMessageListener(
    subModule = "order",
    manualAck = true
)
public class ManualAckHandler implements BpmMessageHandler {
    @Override
    public void handleMessage(BaseBpmMessageDTO message, BpmMessageContext context) throws IOException {
        try {
            // 处理消息...
            context.ack(); // 成功确认
        } catch (Exception e) {
            context.nack(true); // 失败，重新入队
            // 或 context.nack(false); // 失败，不重新入队
        }
    }
}
```

### 消费者标签

框架自动生成有意义的消费者标签，便于在RabbitMQ管理界面识别：

```
bpm-服务名-子模块-环境-主机名-时间戳
```

例如：`bpm-order-service-payment-prod-app01-3578`

### 异常处理和重试机制

框架内置了消息处理异常重试机制：

1. 当消息处理失败时，可以选择是否重新入队
2. 配置`maxRetries`设置最大重试次数
3. 超过最大重试次数后，消息将被丢弃或发送到死信队列

## 配置参考

| 配置项 | 说明 | 默认值 |
|-------|------|-------|
| jasolar.bpm.enabled | 启用BPM模块 | true |
| jasolar.bpm.publisher-enabled | 启用发布者 | true |
| jasolar.bpm.consumer-enabled | 启用消费者 | true |
| jasolar.bpm.current-module | 当前服务模块名 | 应用名 |
| jasolar.bpm.exchange-name | 交换机名称 | BPM.TOPIC |
| jasolar.bpm.queue-prefix | 队列名前缀 | BPM |
| jasolar.bpm.routing-key-prefix | 路由键前缀 | BPM |
| jasolar.bpm.consumers[].subModule | 子模块名 | - |
| jasolar.bpm.consumers[].routingKeys | 路由键列表 | - |
| jasolar.bpm.consumers[].queueName | 队列名（可选） | 自动生成 |
| jasolar.bpm.consumers[].concurrency | 并发消费者数 | 1 |
| jasolar.bpm.consumers[].manualAck | 手动确认 | true |
| jasolar.bpm.consumers[].requeueOnFail | 失败重新入队 | true |
| jasolar.bpm.consumers[].maxRetries | 最大重试次数 | 3 |

## 最佳实践

1. **使用注解方式定义处理器**：更直观、更易维护
2. **遵循子模块命名规范**：使用有意义的子模块名称
3. **合理设置并发数**：根据业务特性和服务器资源
4. **正确处理消息确认**：避免消息丢失和重复处理
5. **日志记录**：在处理器中记录关键操作和异常


