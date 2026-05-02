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
				DBMigrations.ensureSchema();
				System.out.println("✅ DB Ready");
			} else {
				System.out.println("⚠️ No DB connection. Running in LIMITED mode.");
			}

		} catch (Throwable t) {
			System.err.println("⚠️ DB startup error ignored:");
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