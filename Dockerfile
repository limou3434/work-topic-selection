# 编译代码(请在本地直接使用 ./mvnw clean package -DskipTests 进行编译避免容器过大, 可以提前使用 mvn wrapper:wrapper 安装包装器来跨环境编译)
FROM openjdk:8-jdk-slim
COPY ./target/*.jar ./app.jar
EXPOSE 8000
CMD ["java", "-jar", "app.jar"]
