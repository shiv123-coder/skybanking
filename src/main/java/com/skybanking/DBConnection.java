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
    private static HikariDataSource dataSource = null;

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // ✅ Priority: Render ENV → .env (local)
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            String poolSizeStr = System.getenv("DB_POOL_SIZE");

            if (dbUrl == null)
                dbUrl = dotenv.get("DB_URL");
            if (dbUser == null)
                dbUser = dotenv.get("DB_USER");
            if (dbPassword == null)
                dbPassword = dotenv.get("DB_PASSWORD");
            if (poolSizeStr == null)
                poolSizeStr = dotenv.get("DB_POOL_SIZE");

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                LOGGER.severe("❌ DB credentials missing. App will start WITHOUT DB.");
                return; // 🚀 Do NOT crash app
            }

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);

            // ✅ SSL (for Supabase)
            config.addDataSourceProperty("sslmode", "require");

            // Pool size
            int poolSize = 10;
            try {
                if (poolSizeStr != null) {
                    poolSize = Integer.parseInt(poolSizeStr);
                }
            } catch (Exception ignored) {
            }

            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.setDriverClassName("org.postgresql.Driver");
            config.setPoolName("SkyBankPool");

            // ✅ THIS WAS MISSING (CRITICAL)
            dataSource = new HikariDataSource(config);

            LOGGER.info("✅ DB Connected with HikariCP");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ DB INIT FAILED — App will still run", e);
            dataSource = null; // 🚀 keep app alive
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DB not initialized. Check environment variables.");
        }
        return dataSource.getConnection();
    }

    public static boolean isAvailable() {
        return dataSource != null;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Pool shutdown");
        }
    }
}