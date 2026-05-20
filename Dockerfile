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
ENV JAVA_OPTS="-Djava.awt.headless=true -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0"

# Dynamically update Tomcat's connector to bind to ${PORT} on 0.0.0.0, optimize pool threads, disable shutdown port, and skip unnecessary JAR scanning
CMD sed -i 's/port="8080"/port="${PORT}" address="0.0.0.0" maxThreads="150" minSpareThreads="10" acceptCount="100" disableUploadTimeout="true"/g' /usr/local/tomcat/conf/server.xml && \
    sed -i 's/port="8005"/port="-1"/g' /usr/local/tomcat/conf/server.xml && \
    echo "tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar" >> /usr/local/tomcat/conf/catalina.properties && \
    echo "tomcat.util.scan.StandardJarScanFilter.jarsToScan=jstl-*.jar,jakarta.servlet.jsp.jstl-*.jar,standard-*.jar,taglibs-*.jar,glassfish-*.jar" >> /usr/local/tomcat/conf/catalina.properties && \
    catalina.sh run