# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM tomcat:10.1-jdk21-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/skybanking.war /usr/local/tomcat/webapps/ROOT.war

# Set default PORT if not provided by Render
ENV PORT=8080
ENV JAVA_OPTS="-Djava.awt.headless=true -XX:+UseG1GC"

# Dynamically update Tomcat's port to match Render's environment
CMD sed -i "s/8080/$PORT/g" /usr/local/tomcat/conf/server.xml && catalina.sh run