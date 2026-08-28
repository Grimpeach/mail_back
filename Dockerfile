# Используем официальный образ Maven с Java 21 для сборки
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy pom.xml first to cache dependency layers
COPY pom.xml .
# Copy the source code
COPY src ./src
# Build the application, skipping tests to speed up the image build
RUN mvn clean package -DskipTests

# Используем легковесный Alpine образ с Java 21 для запуска (весит в 3 раза меньше)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Копируем скомпилированный jar файл через wildcard
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]