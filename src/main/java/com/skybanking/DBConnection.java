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
    private static final HikariDataSource dataSource;

    static {
        try {
            // ✅ Load .env for LOCAL
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // ✅ ENV priority:
            // 1. System ENV (Render)
            // 2. .env file (local)
            String dbUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : dotenv.get("DB_URL");
            String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : dotenv.get("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD")
                    : dotenv.get("DB_PASSWORD");
            String poolSizeStr = System.getenv("DB_POOL_SIZE") != null ? System.getenv("DB_POOL_SIZE")
                    : dotenv.get("DB_POOL_SIZE");

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new IllegalStateException("❌ Missing DB credentials");
            }

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);

            // 🔥 REQUIRED FOR SUPABASE
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");

            // 🚀 Performance tuning
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            // Pool config
            int poolSize = 10;
            if (poolSizeStr != null) {
                try {
                    poolSize = Integer.parseInt(poolSizeStr);
                } catch (Exception ignored) {
                }
            }

            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.setDriverClassName("org.postgresql.Driver");
            config.setPoolName("SkyBankPool");

            dataSource = new HikariDataSource(config);

            LOGGER.info("✅ DB Connected with HikariCP Pool");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ DB INIT FAILED", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Pool shutdown");
        }
    }
}