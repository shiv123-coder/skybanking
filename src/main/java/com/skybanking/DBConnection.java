package com.skybanking;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static volatile HikariDataSource dataSource = null;
    private static HikariConfig config = null;

    static {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl = getEnv("DB_URL", dotenv);
            String dbUser = getEnv("DB_USER", dotenv);
            String dbPassword = getEnv("DB_PASSWORD", dotenv);
            String poolSizeStr = getEnv("DB_POOL_SIZE", dotenv);

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                LOGGER.severe("❌ DB credentials missing. Production settings cannot be initialized.");
            } else {
                config = new HikariConfig();
                config.setJdbcUrl(dbUrl);
                config.setUsername(dbUser);
                config.setPassword(dbPassword);

                // Supabase / cloud DB SSL
                config.addDataSourceProperty("sslmode", "require");

                int poolSize = 10;
                try {
                    if (poolSizeStr != null)
                        poolSize = Integer.parseInt(poolSizeStr);
                } catch (Exception ignored) {
                }

                config.setMaximumPoolSize(poolSize);
                config.setMinimumIdle(Math.min(5, poolSize));
                config.setConnectionTimeout(10000); // 10 seconds fail-fast
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setValidationTimeout(3000);
                config.setLeakDetectionThreshold(15000); // 15 seconds connection leak warnings

                // PostgreSQL prepared statement caching optimizations
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");

                config.setDriverClassName("org.postgresql.Driver");

                // ✅ Prevent startup crash if DB is unreachable
                config.setInitializationFailTimeout(-1);

                LOGGER.info("✅ Database parameters loaded and verified from environment.");
            }

        } catch (Throwable t) { // ⚠️ catch EVERYTHING
            LOGGER.log(Level.SEVERE, "❌ DB CONFIG LOADING FAILED", t);
            config = null;
        }
    }

    private static String getEnv(String key, Dotenv dotenv) {
        String val = System.getenv(key);
        return val != null ? val : dotenv.get(key);
    }

    public static Connection getConnection() throws SQLException {
        if (config == null) {
            throw new SQLException("Database is not configured.");
        }
        if (dataSource == null) {
            synchronized (DBConnection.class) {
                if (dataSource == null) {
                    try {
                        LOGGER.info("🚀 Lazily initializing HikariCP connection pool...");
                        dataSource = new HikariDataSource(config);
                        LOGGER.info("✅ HikariCP pool initialized successfully");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "❌ Failed to initialize HikariCP pool", e);
                        throw new SQLException("Database connection failed", e);
                    }
                }
            }
        }
        return dataSource.getConnection();
    }

    public static boolean isAvailable() {
        return config != null;
    }

    public static void shutdown() {
        try {
            synchronized (DBConnection.class) {
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                    dataSource = null;
                    LOGGER.info("✅ Pool shutdown");
                }
            }
        } catch (Exception ignored) {
        }
    }
}