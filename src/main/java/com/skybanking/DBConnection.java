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
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl = getEnv("DB_URL", dotenv);
            String dbUser = getEnv("DB_USER", dotenv);
            String dbPassword = getEnv("DB_PASSWORD", dotenv);
            String poolSizeStr = getEnv("DB_POOL_SIZE", dotenv);

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                LOGGER.severe("❌ DB credentials missing. Running WITHOUT database.");
                dataSource = null;
            }

            LOGGER.info("🔍 DB URL = " + dbUrl);
            LOGGER.info("🔍 DB USER = " + dbUser);

            HikariConfig config = new HikariConfig();
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
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.setDriverClassName("org.postgresql.Driver");

            // ✅ Prevent startup crash if DB is unreachable
            config.setInitializationFailTimeout(-1);

            dataSource = new HikariDataSource(config);

            LOGGER.info("✅ HikariCP initialized (DB may connect lazily)");

        } catch (Throwable t) { // ⚠️ catch EVERYTHING
            LOGGER.log(Level.SEVERE, "❌ DB INIT FAILED — continuing without DB", t);
            dataSource = null;
        }
    }

    private static String getEnv(String key, Dotenv dotenv) {
        String val = System.getenv(key);
        return val != null ? val : dotenv.get(key);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DB not available");
        }
        return dataSource.getConnection();
    }

    public static boolean isAvailable() {
        return dataSource != null;
    }

    public static void shutdown() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                LOGGER.info("✅ Pool shutdown");
            }
        } catch (Exception ignored) {
        }
    }
}