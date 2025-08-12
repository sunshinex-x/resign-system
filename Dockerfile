FROM openjdk:17-jdk-slim

WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    unzip \
    zip \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建临时目录
RUN mkdir -p /tmp/resign

# 复制JAR文件
COPY target/resign-system-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]