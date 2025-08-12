# 移动端安装包重签名服务

## 项目介绍

本项目是一个移动端安装包重签名服务，支持iOS、Android和鸿蒙三种系统的安装包重签名。服务采用异步处理方式，通过RabbitMQ进行任务调度，重签名后的安装包存储在MinIO对象存储中。

## 技术栈

- Spring Boot 3.x
- MySQL
- Redis
- RabbitMQ
- MinIO
- MyBatis-Plus

## 功能特性

- 支持iOS、Android和鸿蒙三种系统的安装包重签名
- 异步处理重签名任务，提高系统吞吐量
- 支持任务状态查询和失败重试
- 支持回调通知，及时获取任务处理结果
- 使用MinIO存储重签名后的安装包，支持高可用和水平扩展

## 系统架构

```
+----------------+      +----------------+      +----------------+
|                |      |                |      |                |
|  外部服务/客户端  +----->+  重签名服务API   +----->+  RabbitMQ队列   |
|                |      |                |      |                |
+----------------+      +----------------+      +-------+--------+
                                                        |
                                                        v
+----------------+      +----------------+      +----------------+
|                |      |                |      |                |
|   MinIO存储    <------+  重签名处理服务   <------+  消息消费者     |
|                |      |                |      |                |
+----------------+      +----------------+      +----------------+
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- MinIO

### 配置说明

1. 修改 `application.yml` 配置文件，设置数据库、Redis、RabbitMQ和MinIO的连接信息
2. 根据实际环境修改重签名工具路径配置

### 编译运行

```bash
# 编译
mvn clean package

# 运行
java -jar target/resign-system-0.0.1-SNAPSHOT.jar
```

## API接口

### 创建重签名任务

```
POST /api/resign/tasks
```

请求参数：

```json
{
  "appType": "IOS",
  "originalPackageUrl": "https://example.com/app.ipa",
  "certificateUrl": "https://example.com/certificate.p12",
  "certificatePassword": "password",
  "callbackUrl": "https://example.com/callback"
}
```

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "appType": "IOS",
    "originalPackageUrl": "https://example.com/app.ipa",
    "status": "PENDING",
    "createTime": "2023-12-01T12:00:00"
  }
}
```

### 查询任务状态

```
GET /api/resign/tasks/{taskId}
```

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "appType": "IOS",
    "originalPackageUrl": "https://example.com/app.ipa",
    "resignedPackageUrl": "https://minio.example.com/resign-apps/resigned/550e8400-e29b-41d4-a716-446655440000.ipa",
    "status": "SUCCESS",
    "createTime": "2023-12-01T12:00:00",
    "updateTime": "2023-12-01T12:05:00"
  }
}
```

### 重试失败任务

```
POST /api/resign/tasks/{taskId}/retry
```

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 分页查询任务

```
GET /api/resign/tasks?current=1&size=10&appType=IOS&status=SUCCESS
```

请求参数：

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| current | int | 否 | 当前页码，默认1 |
| size | int | 否 | 每页大小，默认10 |
| appType | string | 否 | 应用类型：IOS、ANDROID、HARMONY |
| status | string | 否 | 任务状态：PENDING、PROCESSING、SUCCESS、FAILED |

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "taskId": "550e8400-e29b-41d4-a716-446655440000",
        "appType": "IOS",
        "originalPackageUrl": "https://example.com/app.ipa",
        "resignedPackageUrl": "https://minio.example.com/resign-apps/resigned/550e8400-e29b-41d4-a716-446655440000.ipa",
        "status": "SUCCESS",
        "createTime": "2023-12-01T12:00:00",
        "updateTime": "2023-12-01T12:05:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 高级查询任务

```
POST /api/resign/tasks/search
```

请求参数：

```json
{
  "current": 1,
  "size": 10,
  "appType": "IOS",
  "status": "SUCCESS",
  "taskId": "550e8400",
  "startTime": "2023-12-01 00:00:00",
  "endTime": "2023-12-31 23:59:59"
}
```

响应结果与分页查询相同。

### 统计任务数量

```
GET /api/resign/tasks/stats
```

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "PENDING": 10,
    "PROCESSING": 5,
    "SUCCESS": 100,
    "FAILED": 2,
    "TOTAL": 117
  }
}
```

### 批量删除任务

```
DELETE /api/resign/tasks/batch
```

请求参数：

```json
[
  "550e8400-e29b-41d4-a716-446655440000",
  "550e8400-e29b-41d4-a716-446655440001"
]
```

响应结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

## 回调通知

当任务处理完成后，系统会向创建任务时指定的回调URL发送POST请求，通知任务处理结果：

```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "originalPackageUrl": "https://example.com/app.ipa",
  "resignedPackageUrl": "https://minio.example.com/resign-apps/resigned/550e8400-e29b-41d4-a716-446655440000.ipa",
  "failReason": null
}
```

## 注意事项

1. 确保重签名工具已正确安装并配置路径
2. iOS重签名需要macOS环境
3. 证书和安装包URL必须可以访问
4. 临时文件目录需要足够的磁盘空间

## 许可证

MIT