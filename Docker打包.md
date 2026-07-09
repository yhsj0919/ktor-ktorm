需要用到linux或者wsl

多架构打包推送，我自己用
docker buildx build \ --no-cache \ --platform linux/amd64,linux/arm64 \ -t yhsj0919/weigh3-mysql:latest \ --push .


本地打包
docker build -t yhsj0919/weigh3-mysql:latest .


制作docker镜像
1️⃣ 打包 Java 项目
生成jar文件

2️⃣ 编写 Dockerfile（核心）
在项目根目录创建 Dockerfile：
```dockerfile

# 1. 基础镜像（JDK 17，推荐）
FROM eclipse-temurin:17-jre

# 2. 设置工作目录
WORKDIR /app

# 3. 拷贝 jar 包
COPY target/*.jar app.jar

# 4. 暴露端口（按你项目来）
EXPOSE 8080

# 5. 启动命令
ENTRYPOINT ["java","-jar","app.jar"]
```

3️⃣ 构建 Docker 镜像
docker build -t my-java-app:1.0 .

查看：

docker images

4️⃣ 运行容器
docker run -d \
-p 8080:8080 \
--name my-java-app \
my-java-app:1.0


访问：

http://localhost:8080