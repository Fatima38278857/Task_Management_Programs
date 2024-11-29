FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn dependency:resolve -e -X
RUN mvn clean package -DskipTests -e -X

# Этап исполнения
FROM openjdk:17-jdk-slim
WORKDIR /app

# Копируем приложение из этапа сборки
COPY --from=builder /app/target/*.jar app.jar
# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]