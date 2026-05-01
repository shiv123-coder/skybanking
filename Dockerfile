# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run (FIXED)
FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/BankingWebApp.war /usr/local/tomcat/webapps/ROOT.war

ENV JAVA_OPTS="-Djava.awt.headless=true -XX:+UseG1GC"

EXPOSE 8080

CMD ["catalina.sh", "run"]