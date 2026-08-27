FROM maven:3.8-openjdk-18 AS builder
WORKDIR /app
# Copy pom.xml first to cache dependency layers
COPY pom.xml .
# Copy the source code
COPY src ./src
# Build the application, skipping tests to speed up the image build
RUN mvn clean package -DskipTests

# Use Eclipse Temurin JRE (industry standard replacement for deprecated openjdk)
FROM eclipse-temurin:18-jre
WORKDIR /app
COPY --from=builder /app/target/social_network-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]