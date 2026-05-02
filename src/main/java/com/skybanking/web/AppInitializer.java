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

		System.out.println("🚀 Starting SkyBanking App...");

		try {
			if (DBConnection.isAvailable()) {
				try {
					DBMigrations.ensureSchema();
					System.out.println("✅ DB Ready");
				} catch (Throwable t) {
					System.err.println("⚠️ DB migration failed, continuing...");
					t.printStackTrace();
				}
			} else {
				System.out.println("⚠️ No DB connection. Running in LIMITED mode.");
			}

		} catch (Throwable t) {
			// 🚨 NEVER allow listener to crash
			System.err.println("⚠️ App startup error ignored:");
			t.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			DBConnection.shutdown();
		} catch (Exception ignored) {
		}
	}
}