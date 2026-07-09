FROM eclipse-temurin:21-jre

# 1. 设置时区为北京时间
ENV TZ=Asia/Shanghai

# 2. 安装 tzdata 并配置时区
RUN apt-get update && apt-get install -y tzdata && \
    ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    dpkg-reconfigure --frontend noninteractive tzdata

# 3. 设置工作目录
WORKDIR /app

# 4. 拷贝 jar 包
COPY libs/*.jar app.jar

# 5. 暴露端口（按你项目来）
EXPOSE 80

# 6. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]