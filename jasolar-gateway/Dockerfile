# 1. 使用官方 OpenJDK 17 轻量级镜像
FROM openjdk:17-jdk-alpine

# 2. 创建日志目录（可选，便于挂载日志卷）
RUN mkdir -p /logs

# 3. 设置工作目录
WORKDIR /app

# 4. 复制 jar 包到镜像中
COPY target/jasolar-gateway.jar app.jar

# 5. 暴露端口（如 8080，按实际端口修改）
EXPOSE 8089

# 6. 启动命令，支持外部配置挂载（ConfigMap 挂载到 /config/）
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.additional-location=file:/config/"]
