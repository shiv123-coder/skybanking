package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.util.DBMigrations;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			System.out.println("🚀 Starting SkyBanking App...");

			if (DBConnection.isAvailable()) {
				DBMigrations.ensureSchema();
				System.out.println("✅ DB Ready");
			} else {
				System.out.println("⚠️ DB not available. App running in LIMITED mode.");
			}

		} catch (Exception e) {
			System.err.println("⚠️ Startup warning (ignored): " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		DBConnection.shutdown();
	}
}