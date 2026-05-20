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
			// FORCE SERVLET CLASS LOADING
			Class.forName("com.skybanking.web.LoginServlet");
			Class.forName("com.skybanking.web.DashboardServlet");
			Class.forName("com.skybanking.web.SignupServlet");

			System.out.println("✅ Servlet classes forced to load");

		} catch (Exception e) {
			System.err.println("⚠️ Servlet preload failed");
			e.printStackTrace();
		}

		try {
			if (DBConnection.isAvailable()) {
				Thread migrationThread = new Thread(() -> {
					try {
						System.out.println("⏳ Running database schema migrations in background thread...");
						DBMigrations.ensureSchema();
						System.out.println("✅ Database schema migrations and seeding completed.");
					} catch (Throwable t) {
						System.err.println("⚠️ Asynchronous DB migration checking failed: " + t.getMessage());
						t.printStackTrace();
					}
				}, "DB-Migration-Thread");
				migrationThread.setDaemon(true);
				migrationThread.start();
				System.out.println("🚀 Asynchronous DB Migration Thread started.");
			} else {
				System.out.println("⚠️ No DB configuration detected. Skipping background migrations.");
			}

		} catch (Throwable t) {
			System.err.println("⚠️ Failed to dispatch DB startup thread:");
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