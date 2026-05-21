# ============================================================
# SkyBanking — Production Dockerfile for Render Deployment
# ============================================================
# Multi-stage build: Maven build → Tomcat 10.1 runtime
# Render injects PORT env var; defaults to 10000 if missing.
# ============================================================

# -------------------- Stage 1: Build --------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven dependencies (layer caching optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the WAR
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------- Stage 2: Runtime --------------------
FROM tomcat:10.1-jdk21-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy the WAR as ROOT (context path /)
COPY --from=build /app/target/skybanking.war /usr/local/tomcat/webapps/ROOT.war

# Default port for Render (Render injects PORT env var at runtime)
ENV PORT=10000

# JVM optimizations for containerized environments
ENV JAVA_OPTS="-Djava.awt.headless=true \
  -XX:+UseG1GC \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=40.0 \
  -Djava.security.egd=file:/dev/./urandom"

# Expose the default port
EXPOSE 10000

# Create the startup entrypoint script
RUN cat > /usr/local/tomcat/bin/docker-entrypoint.sh << 'ENTRY_SCRIPT'
#!/bin/bash
set -e

echo "============================================"
echo "  SkyBanking — Container Startup"
echo "============================================"

# --------------------------------------------------
# 1. Validate and sanitize PORT
# --------------------------------------------------
if [ -z "$PORT" ]; then
  export PORT=10000
  echo "⚠️  PORT not set, defaulting to 10000"
fi

# Ensure PORT is a valid integer between 1-65535
if ! echo "$PORT" | grep -qE '^[0-9]+$' || [ "$PORT" -lt 1 ] || [ "$PORT" -gt 65535 ]; then
  echo "❌ Invalid PORT='$PORT'. Must be 1-65535. Falling back to 10000."
  export PORT=10000
fi

echo "✅ PORT=${PORT}"

# --------------------------------------------------
# 2. Patch server.xml — connector port + address
# --------------------------------------------------
SERVER_XML="/usr/local/tomcat/conf/server.xml"

# Replace the default 8080 connector with the correct PORT and bind to 0.0.0.0
sed -i "s|Connector port=\"8080\"|Connector port=\"${PORT}\" address=\"0.0.0.0\"|g" "$SERVER_XML"

# Also handle case where port was already changed in a previous run
sed -i -E "s|Connector port=\"[0-9]+\"|Connector port=\"${PORT}\"|g" "$SERVER_XML"

# Ensure address binding is present (if not already added above)
if ! grep -q 'address="0.0.0.0"' "$SERVER_XML"; then
  sed -i "s|Connector port=\"${PORT}\"|Connector port=\"${PORT}\" address=\"0.0.0.0\"|g" "$SERVER_XML"
fi

# Disable shutdown port (security: prevent remote shutdown)
sed -i 's|port="8005"|port="-1"|g' "$SERVER_XML"

echo "✅ Tomcat connector bound to 0.0.0.0:${PORT}"

# --------------------------------------------------
# 3. Optimize Tomcat startup (skip JAR scanning)
# --------------------------------------------------
CATALINA_PROPS="/usr/local/tomcat/conf/catalina.properties"
if ! grep -q "jarsToSkip=\*\.jar" "$CATALINA_PROPS"; then
  echo "tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar" >> "$CATALINA_PROPS"
  echo "tomcat.util.scan.StandardJarScanFilter.jarsToScan=jstl-*.jar,jakarta.servlet.jsp.jstl-*.jar,standard-*.jar,taglibs-*.jar,glassfish-*.jar" >> "$CATALINA_PROPS"
fi

# --------------------------------------------------
# 4. Validate required environment variables
# --------------------------------------------------
echo "--------------------------------------------"
echo "  Environment Validation"
echo "--------------------------------------------"

MISSING_VARS=""

# Database
if [ -n "$DB_URL" ]; then
  # Mask the URL for logging (show host only)
  DB_HOST=$(echo "$DB_URL" | sed -E 's|.*//([^/:]+).*|\1|' 2>/dev/null || echo "unknown")
  echo "✅ DB_URL set (host: ${DB_HOST})"
else
  echo "⚠️  DB_URL not set — database features will fail"
  MISSING_VARS="${MISSING_VARS} DB_URL"
fi

[ -n "$DB_USER" ] && echo "✅ DB_USER set" || { echo "⚠️  DB_USER not set"; MISSING_VARS="${MISSING_VARS} DB_USER"; }
[ -n "$DB_PASSWORD" ] && echo "✅ DB_PASSWORD set (masked)" || { echo "⚠️  DB_PASSWORD not set"; MISSING_VARS="${MISSING_VARS} DB_PASSWORD"; }

# Email / SMTP
[ -n "$SMTP_EMAIL" ] && echo "✅ SMTP_EMAIL set" || echo "⚠️  SMTP_EMAIL not set — email features disabled"
[ -n "$SMTP_PASSWORD" ] && echo "✅ SMTP_PASSWORD set (masked)" || echo "⚠️  SMTP_PASSWORD not set — email features disabled"

# Stripe
[ -n "$STRIPE_SECRET_KEY" ] && echo "✅ STRIPE_SECRET_KEY set (masked)" || echo "⚠️  STRIPE_SECRET_KEY not set — payment features disabled"

# Admin
[ -n "$ADMIN_PASSWORD" ] && echo "✅ ADMIN_PASSWORD set (masked)" || echo "⚠️  ADMIN_PASSWORD not set — using default"

echo "--------------------------------------------"

if [ -n "$MISSING_VARS" ]; then
  echo "⚠️  Missing critical vars:${MISSING_VARS}"
  echo "   App will start but some features may not work."
fi

echo "============================================"
echo "  Starting Tomcat on port ${PORT}..."
echo "============================================"

# --------------------------------------------------
# 5. Start Tomcat
# --------------------------------------------------
exec catalina.sh run
ENTRY_SCRIPT

RUN chmod +x /usr/local/tomcat/bin/docker-entrypoint.sh

# Use the entrypoint script
CMD ["/usr/local/tomcat/bin/docker-entrypoint.sh"]