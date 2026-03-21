FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY service/pom.xml ./service/pom.xml
COPY service/coupon-service/pom.xml ./service/coupon-service/pom.xml
COPY service/coupon-service/coupon-core/pom.xml ./service/coupon-service/coupon-core/pom.xml
COPY service/coupon-service/coupon-api/pom.xml ./service/coupon-service/coupon-api/pom.xml
COPY service/coupon-service/coupon-worker/pom.xml ./service/coupon-service/coupon-worker/pom.xml
COPY service/notification-service/pom.xml ./service/notification-service/pom.xml
COPY service/order-service/pom.xml ./service/order-service/pom.xml
COPY service/payment-service/pom.xml ./service/payment-service/pom.xml
COPY service/product-service/pom.xml ./service/product-service/pom.xml
COPY service/ranking-service/pom.xml ./service/ranking-service/pom.xml
COPY service/review-service/pom.xml ./service/review-service/pom.xml
COPY service/search-service/pom.xml ./service/search-service/pom.xml
COPY service/user-service/pom.xml ./service/user-service/pom.xml
COPY service/product-service ./service/product-service
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl service/product-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/service/product-service/target/*.jar app.jar
EXPOSE 9004
ENTRYPOINT ["java", "-jar", "app.jar"]
